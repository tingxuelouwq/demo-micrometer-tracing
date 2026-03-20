package com.kevin.demo.tracing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
//@ImportHttpServices(group = "demo", basePackages = {"com.kevin.demo.tracing"})
public class DemoMicrometerTracingApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoMicrometerTracingApplication.class, args);
    }

}
