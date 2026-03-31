package com.kevin.demo.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Hooks;

@SpringBootApplication
public class GatewayApplication {
    public static void main(String[] args) {
        // 关键：启用 Reactor 上下文自动传播
        Hooks.enableAutomaticContextPropagation();

        SpringApplication.run(GatewayApplication.class, args);
    }
}
