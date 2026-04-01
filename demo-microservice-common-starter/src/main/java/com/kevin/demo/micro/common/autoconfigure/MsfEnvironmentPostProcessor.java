package com.kevin.demo.micro.common.autoconfigure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

/**
 * EnvironmentPostProcessor that loads msf default configuration early in the startup process.
 * This ensures that properties are available before auto-configuration conditions are evaluated.
 */
@SuppressWarnings("removal")
public class MsfEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String MSF_CONFIG = "application-msf.yml";
    private static final String PROPERTY_SOURCE_NAME = "msf-defaults";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        ClassPathResource resource = new ClassPathResource(MSF_CONFIG);
        if (!resource.exists()) {
            return;
        }

        YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
        try {
            List<PropertySource<?>> propertySources = loader.load(PROPERTY_SOURCE_NAME, resource);
            for (PropertySource<?> propertySource : propertySources) {
                environment.getPropertySources().addFirst(propertySource);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load MSF configuration: " + MSF_CONFIG, e);
        }
    }
}
