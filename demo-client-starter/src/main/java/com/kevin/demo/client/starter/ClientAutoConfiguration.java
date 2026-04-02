package com.kevin.demo.client.starter;

import com.kevin.demo.client.starter.echo.EchoClient;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.web.service.registry.ImportHttpServices;

import java.io.IOException;
import java.util.Properties;

@AutoConfiguration
@PropertySource(value = "classpath:application-client-starter.yml", factory = ClientAutoConfiguration.YamlPropertySourceFactory.class)
@ImportHttpServices(group = "demo-echo-service", types = { EchoClient.class })
//@ImportHttpServices(group = "demo-user-service", types = { UserClient.class })
public class ClientAutoConfiguration {

    public static class YamlPropertySourceFactory implements PropertySourceFactory {
        @Override
        public org.springframework.core.env.PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
            YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
            factory.setResources(resource.getResource());
            Properties properties = factory.getObject();
            return new PropertiesPropertySource(resource.getResource().getFilename(), properties);
        }
    }
}
