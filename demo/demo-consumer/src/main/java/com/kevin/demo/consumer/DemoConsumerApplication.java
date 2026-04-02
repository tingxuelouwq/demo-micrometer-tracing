package com.kevin.demo.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.service.registry.ImportHttpServices;
import reactor.core.publisher.Hooks;

@SpringBootApplication
@ImportHttpServices(group = "demo-echo-service", types = {EchoClient.class})
public class DemoConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoConsumerApplication.class, args);
    }

}
