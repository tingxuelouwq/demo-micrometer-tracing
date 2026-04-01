package com.kevin.demo.micro.common.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.Encoder;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 自定义 Kafka Logback Appender，用于将日志异步发送到 Kafka。
 *
 * <p>在 {@code logback-spring.xml} 中配置使用，支持从 {@code application.yml} 读取 Kafka 连接配置。</p>
 *
 * <p><b>核心特性：</b></p>
 * <ul>
 *   <li>从 {@code application.yml} 的 {@code logging.kafka} 配置读取 {@code bootstrap-servers} 和 {@code topic}</li>
 *   <li>自动集成 Micrometer Tracing → Brave → MDC → Logback → Kafka</li>
 *   <li>无需手动处理 traceId/spanId，MDC 中已自动包含</li>
 *   <li>使用异步方式发送日志到 Kafka，避免阻塞应用</li>
 * </ul>
 *
 * <p><b>配置数据流：</b></p>
 * <pre>
 * application.yml (logging.kafka.bootstrap-servers)
 *     ↓
 * springProperty (LOGGING_KAFKA_BOOTSTRAP_SERVERS)
 *     ↓
 * logback-spring.xml (${LOGGING_KAFKA_BOOTSTRAP_SERVERS})
 *     ↓
 * KafkaAppender.setBootstrapServers()
 *     ↓
 * KafkaProducer 连接到 Kafka
 * </pre>
 *
 * <p><b>实现说明：</b></p>
 * <p>Micrometer Tracing 默认实现是 Brave。虽然 Spring Boot 4.0 未来推荐使用 OpenTelemetry，但目前集成上还是 Brave 更为成熟。
 * 若强制使用 OpenTelemetry，需要排除 Brave 依赖，或者等未来版本更新。现阶段如果类路径同时存在 Brave 和 OTel，Brave 优先。</p>
 *
 * <p><b>配置示例（application.yml）：</b></p>
 * <pre>
 * logging:
 *   kafka:
 *     bootstrap-servers: ${KAFKA_SERVER:localhost:9092}
 *     topic: ${KAFKA_TOPIC:app-logs}
 * </pre>
 *
 * <p><b>配置示例（logback-spring.xml）：</b></p>
 * <pre>
 * &lt;springProperty scope="context" name="LOGGING_KAFKA_BOOTSTRAP_SERVERS"
 *     source="logging.kafka.bootstrap-servers" defaultValue="localhost:9092"/&gt;
 * &lt;springProperty scope="context" name="LOGGING_KAFKA_TOPIC"
 *     source="logging.kafka.topic" defaultValue="app-logs"/&gt;
 *
 * &lt;appender name="KAFKA" class="com.kevin.demo.micro.common.logging.KafkaAppender"&gt;
 *     &lt;bootstrapServers&gt;${LOGGING_KAFKA_BOOTSTRAP_SERVERS}&lt;/bootstrapServers&gt;
 *     &lt;topic&gt;${LOGGING_KAFKA_TOPIC}&lt;/topic&gt;
 *     &lt;encoder&gt;...&lt;/encoder&gt;
 * &lt;/appender&gt;
 * </pre>
 *
 * @author 王琪
 * @since 2026/4/1 15:51
 */
public class KafkaAppender extends AppenderBase<ILoggingEvent> {

    private String bootstrapServers = "localhost:9092";
    private String topic = "app-logs";
    private Encoder<ILoggingEvent> encoder;

    private KafkaProducer<String, byte[]> producer;
    private BlockingQueue<byte[]> queue;
    private Thread senderThread;
    private volatile boolean running = true;

    // Producer 配置
    private int batchSize = 16384;
    private int lingerMs = 100;
    private int bufferMemory = 33554432;
    private int queueSize = 10000;

    @Override
    public void start() {
        if (encoder == null) {
            addError("No encoder set for the appender named [" + name + "]");
            return;
        }

        try {
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
            props.put(ProducerConfig.ACKS_CONFIG, "0"); // 不等待确认，提高性能
            props.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
            props.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
            props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);
            // 连接失败时快速失败，不阻塞应用启动
            props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 1000);

            producer = new KafkaProducer<>(props);
            queue = new LinkedBlockingQueue<>(queueSize);

            // 启动异步发送线程
            senderThread = new Thread(this::sendLoop, "kafka-log-sender");
            senderThread.setDaemon(true);
            senderThread.start();

            encoder.start();
            super.start();
            addInfo("KafkaAppender started, sending logs to " + bootstrapServers + " topic: " + topic);
        } catch (Exception e) {
            addError("Failed to start KafkaAppender", e);
        }
    }

    @Override
    public void stop() {
        running = false;
        if (senderThread != null) {
            senderThread.interrupt();
            try {
                senderThread.join(5000);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
        if (producer != null) {
            producer.close();
        }
        if (encoder != null) {
            encoder.stop();
        }
        super.stop();
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (!isStarted()) {
            return;
        }
        try {
            byte[] encoded = encoder.encode(event);
            // 非阻塞方式入队，队列满时丢弃日志
            if (!queue.offer(encoded)) {
                addWarn("Kafka log queue is full, dropping log message");
            }
        } catch (Exception e) {
            addError("Failed to encode log event", e);
        }
    }

    private void sendLoop() {
        while (running) {
            try {
                byte[] data = queue.poll(100, TimeUnit.MILLISECONDS);
                if (data != null) {
                    producer.send(new ProducerRecord<>(topic, data), (metadata, exception) -> {
                        if (exception != null) {
                            addError("Failed to send log to Kafka", exception);
                        }
                    });
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                addError("Error in Kafka sender loop", e);
            }
        }
        // 发送剩余的日志
        drainQueue();
    }

    private void drainQueue() {
        byte[] data;
        while ((data = queue.poll()) != null) {
            try {
                producer.send(new ProducerRecord<>(topic, data));
            } catch (Exception e) {
                addError("Failed to drain log queue", e);
            }
        }
        producer.flush();
    }

    // Setters for configuration
    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setEncoder(Encoder<ILoggingEvent> encoder) {
        this.encoder = encoder;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public void setLingerMs(int lingerMs) {
        this.lingerMs = lingerMs;
    }

    public void setBufferMemory(int bufferMemory) {
        this.bufferMemory = bufferMemory;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }
}
