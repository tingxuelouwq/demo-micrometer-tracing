package com.kevin.demo.tracing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.service.registry.ImportHttpServices;

@SpringBootApplication
@ImportHttpServices(group = "user-client", types = {UserClient.class})
@ImportHttpServices(group = "echo-client", types = {EchoClient.class})
public class DemoMicrometerTracingApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoMicrometerTracingApplication.class, args);
    }

}
