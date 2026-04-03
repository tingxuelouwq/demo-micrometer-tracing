package com.kevin.demo.microservice.starter.autoconfigure;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationPredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * 过滤 Actuator 端点的链路追踪
 * @author 王琪
 * @since 2026/3/31 16:58
 */
@AutoConfiguration
@ConditionalOnClass(ObservationPredicate.class)
@ConditionalOnWebApplication
public class BraveTracingConfiguration {

    private static final Logger log = LoggerFactory.getLogger(BraveTracingConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(name = "actuatorServerContextPredicate")
    public ObservationPredicate actuatorServerContextPredicate(
            @Value("${tracing.filter.exclude-paths:/actuator}") String[] excludePaths) {

        log.info("Initializing tracing filter, exclude paths: {}", Arrays.toString(excludePaths));

        return (name, context) -> {
            String uri = extractUri(context);
            if (uri == null) return true;

            boolean shouldObserve = Arrays.stream(excludePaths)
                    .noneMatch(uri::startsWith);

            if (!shouldObserve && log.isTraceEnabled()) {
                log.trace("Filtered tracing for URI: {}", uri);
            }

            return shouldObserve;
        };
    }

    private String extractUri(Observation.Context context) {
        try {
            if (context instanceof org.springframework.http.server.observation.ServerRequestObservationContext servletCtx) {
                if (servletCtx.getCarrier() != null) {
                    return servletCtx.getCarrier().getRequestURI();
                }
            } else if (context instanceof org.springframework.http.server.reactive.observation.ServerRequestObservationContext reactiveCtx) {
                if (reactiveCtx.getCarrier() != null) {
                    return reactiveCtx.getCarrier().getURI().getPath();
                }
            }
        } catch (Exception e) {
            // 防御性处理，避免异常中断观测
        }
        return null;
    }
}


