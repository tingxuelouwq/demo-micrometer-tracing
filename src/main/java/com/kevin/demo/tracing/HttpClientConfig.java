package com.kevin.demo.tracing;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * Spring Boot 3.2+ 极简配置
 * 利用 HttpServiceProxyFactory 便捷构建器
 */
@Configuration
public class HttpClientConfig {

    /**
     * 创建 UserClient 代理实例
     * 自动继承 application.yml 中的配置:
     * - base-url: lb://demo-micrometer-tracing (负载均衡)
     * - connect-timeout: 2s
     * - read-timeout: 5s
     */
    @Bean
    UserClient userClient(RestClient.Builder restClientBuilder) {
        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClientBuilder.build()))
                .build()
                .createClient(UserClient.class);
    }
}