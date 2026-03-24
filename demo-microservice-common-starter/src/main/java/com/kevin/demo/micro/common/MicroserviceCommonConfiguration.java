package com.kevin.demo.micro.common;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.PropertySource;

@AutoConfiguration
@PropertySource("classpath:micro-common.properties")
public class MicroserviceCommonConfiguration {
}
