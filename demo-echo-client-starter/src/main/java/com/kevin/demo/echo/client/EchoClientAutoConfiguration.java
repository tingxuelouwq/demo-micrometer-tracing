package com.kevin.demo.echo.client;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.web.service.registry.ImportHttpServices;

@AutoConfiguration
@ImportHttpServices(group = "demo-echo-service", types = { EchoClient.class })
public class EchoClientAutoConfiguration {
}
