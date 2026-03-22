package com.kevin.demo.echo.client;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@AutoConfigureAfter(name = {
        "io.github.resilience4j.springboot3.autoconfigure.Resilience4jAutoConfiguration"
})
@ImportHttpServices(group = "echo-client", types = { EchoClient.class })
public class EchoClientAutoConfiguration {

    @Bean
    public EchoClientService echoClientService(EchoClient echoClient,
                                               CircuitBreakerRegistry cbRegistry,
                                               RetryRegistry retryRegistry,
                                               TimeLimiterRegistry timeLimiterRegistry) {
        return new EchoClientService(echoClient, cbRegistry, retryRegistry, timeLimiterRegistry);
    }
}
