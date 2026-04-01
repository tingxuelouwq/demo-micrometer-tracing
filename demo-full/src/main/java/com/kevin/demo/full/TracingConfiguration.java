package com.kevin.demo.full;


import io.micrometer.observation.ObservationPredicate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.server.reactive.observation.ServerRequestObservationContext;
import zipkin2.reporter.BytesMessageSender;
import zipkin2.reporter.brave.AsyncZipkinSpanHandler;
import zipkin2.reporter.kafka.KafkaSender;

import java.util.Objects;


@Configuration
public class TracingConfiguration {

    /**
     * 默认KafkaProperties（对应spring.kafka）
     * 标记@Primary：让Spring自动配置类优先注入这个Bean，解决歧义问题
     */
    @Primary
    @Bean(name = "kafkaProperties")
    @ConfigurationProperties(prefix = "spring.kafka")
    public KafkaProperties defaultKafkaProperties() {
        return new KafkaProperties();
    }

    /**
     * 自定义链路追踪KafkaProperties（对应tracing.kafka）
     * 指定唯一名称tracingKafkaProperties，避免与默认Bean冲突
     */
    @Bean(name = "tracingKafkaProperties")
    @ConfigurationProperties(prefix = "tracing.kafka")
    public KafkaProperties tracingKafkaProperties() {
        return new KafkaProperties();
    }

    /** 配置micrometer tracing将日志发送至kafka **/
    @Primary
    @Bean
    public BytesMessageSender kafkaSender(@Qualifier("tracingKafkaProperties") KafkaProperties properties) {
        return KafkaSender.newBuilder()
                .bootstrapServers(String.join(",", properties.getBootstrapServers()))
                .topic(Objects.requireNonNull(properties.getTemplate().getDefaultTopic())) // Default topic used by Zipkin Kafka collector
                .build();
    }

    @Bean
    public AsyncZipkinSpanHandler zipkinSpanHandler(BytesMessageSender kafkaSender) {

        return AsyncZipkinSpanHandler.create(kafkaSender);
    }

    /**
     * 不记录/actuator下的请求
     * @return
     */
    @Bean
    public ObservationPredicate actuatorServerContextPredicate() {
        return (name, context) -> {
            if (context instanceof ServerRequestObservationContext serverContext) {
                // Exclude tracing for requests starting with /actuator
                String uri = serverContext.getCarrier().getURI().getPath();
                return !uri.startsWith("/actuator");
            } else if (context instanceof org.springframework.http.server.observation.ServerRequestObservationContext serverContext) {
                return !serverContext.getCarrier().getRequestURI().startsWith("/actuator");
            }
            // For other observation contexts, allow tracing (or apply other logic)
            return true;
        };
    }
}

