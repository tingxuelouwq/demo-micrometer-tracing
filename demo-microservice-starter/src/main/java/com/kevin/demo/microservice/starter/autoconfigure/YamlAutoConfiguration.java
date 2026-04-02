package com.kevin.demo.microservice.starter.autoconfigure;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author 王琪
 * @since 2026/4/1 15:57
 */
@AutoConfiguration
@PropertySource(value = "classpath:application-microservice-starter.yml", factory = YamlAutoConfiguration.YamlPropertySourceFactory.class)
public class YamlAutoConfiguration {

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
