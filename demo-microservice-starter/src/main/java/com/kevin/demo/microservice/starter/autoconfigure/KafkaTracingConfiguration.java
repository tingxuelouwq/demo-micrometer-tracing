package com.kevin.demo.microservice.starter.autoconfigure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import zipkin2.reporter.BytesMessageSender;
import zipkin2.reporter.brave.AsyncZipkinSpanHandler;
import zipkin2.reporter.kafka.KafkaSender;

import java.util.Map;
import java.util.Optional;

/**
 * 多数据源kafka，将分布式链路追踪kafka和业务消息kafka分离
 * @author 王琪
 * @since 2026/4/3 14:26
 */
@AutoConfiguration
public class KafkaTracingConfiguration {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 业务消息kafka
     */
    @Primary
    @Bean
    @ConfigurationProperties("spring.kafka")
    public KafkaProperties kafkaProperties() {
        return new KafkaProperties();
    }

    /**
     * 链路追踪kafka
     */
    @Bean
    @ConfigurationProperties("tracing.kafka")
    public KafkaProperties tracingKafkaProperties() {
        return new KafkaProperties();
    }

    @Bean(destroyMethod = "close")
    public BytesMessageSender zipkinKafkaSender(
            @Qualifier("tracingKafkaProperties") KafkaProperties props) {
        String servers = String.join(",", props.getBootstrapServers());
        String topic = Optional.ofNullable(props.getTemplate().getDefaultTopic()).orElse("zipkin");

        logger.info("Initializing Zipkin Kafka sender: servers={}, topic={}", servers, topic);

        return KafkaSender.newBuilder()
                .bootstrapServers(servers)
                .topic(topic)
                // 容忍丢失的追踪数据，acks=1：Leader 确认写入即返回，快速确认；linger.ms=10：延迟10ms批量发送，提高吞吐；compression.type=lz4：使用lz4算法压缩消息
                .overrides(Map.of(
                        "acks", "1",
                        "linger.ms", "10",
                        "compression.type", "lz4"
                ))
                .build();
    }

    @Bean(destroyMethod = "close")
    public AsyncZipkinSpanHandler zipkinSpanHandler(
            @Qualifier("zipkinKafkaSender") BytesMessageSender sender) {
        return AsyncZipkinSpanHandler.create(sender);
    }
}
