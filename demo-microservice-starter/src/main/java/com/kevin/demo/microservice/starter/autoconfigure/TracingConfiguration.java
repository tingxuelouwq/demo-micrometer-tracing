package com.kevin.demo.microservice.starter.autoconfigure;

import io.micrometer.observation.ObservationPredicate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author 王琪
 * @since 2026/3/31 16:58
 */
@AutoConfiguration
public class TracingConfiguration {

    /**
     * 不记录 /actuator 下的请求
     */
    @Bean
    public ObservationPredicate actuatorServerContextPredicate() {
        return (name, context) -> {
            if (context instanceof org.springframework.http.server.observation.ServerRequestObservationContext serverContext) {
                // Servlet 环境
                String uri = serverContext.getCarrier().getRequestURI();
                return !uri.startsWith("/actuator");
            } else if (context instanceof org.springframework.http.server.reactive.observation.ServerRequestObservationContext serverContext) {
                // WebFlux 环境
                String uri = serverContext.getCarrier().getURI().getPath();
                return !uri.startsWith("/actuator");
            }
            return true;
        };
    }
}


