package com.kevin.demo.full;

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
 * 自定义 Kafka Logback Appender
 * 使用异步方式发送日志到 Kafka，避免阻塞应用
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
