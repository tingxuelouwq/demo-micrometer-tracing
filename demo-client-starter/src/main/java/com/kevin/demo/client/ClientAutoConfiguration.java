package com.kevin.demo.client;

import com.kevin.demo.client.echo.EchoClient;
import com.kevin.demo.client.user.UserClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.service.registry.ImportHttpServices;

@AutoConfiguration
@ImportHttpServices(group = "demo-echo-service", types = { EchoClient.class })
@ImportHttpServices(group = "demo-user-service", types = { UserClient.class })
@PropertySource("classpath:client-common.properties")
public class ClientAutoConfiguration {
}
