package com.kevin.demo.full;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.service.registry.ImportHttpServices;

@SpringBootApplication
@ImportHttpServices(group = "echo-client", types = {EchoClient.class})
public class DemoFullApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoFullApplication.class, args);
    }

}
