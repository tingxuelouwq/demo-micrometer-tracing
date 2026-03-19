package com.kevin.demo.tracing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ReactorResourceFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    // ==================== 基础连接池配置 ====================
    @Value("${webclient.max-connections:200}")
    private int maxConnections;

    @Value("${webclient.pending-acquire-max-count:500}")
    private int pendingAcquireMaxCount;

    // ==================== 超时配置 ====================
    @Value("${webclient.connect-timeout:2000}")
    private int connectTimeoutMillis;

    @Value("${webclient.read-timeout:5000}")
    private int readTimeoutSeconds;

    @Value("${webclient.write-timeout:5000}")
    private int writeTimeoutSeconds;

    // ==================== 连接池生命周期配置 ====================
    @Value("${webclient.max-idle-time-seconds:30}")
    private int maxIdleTimeSeconds;

    @Value("${webclient.max-life-time-minutes:5}")
    private int maxLifeTimeMinutes;

    @Value("${webclient.pending-acquire-timeout-seconds:10}")
    private int pendingAcquireTimeoutSeconds;

    @Value("${webclient.evict-in-background-seconds:30}")
    private int evictInBackgroundSeconds;

    /**
     * Reactor Netty 连接池配置（高性能 HTTP 客户端）
     * 作用：配置超时、连接池等底层参数
     */
    @Bean
    @ConditionalOnMissingBean
    public ReactorResourceFactory reactorResourceFactory() {
        ReactorResourceFactory factory = new ReactorResourceFactory();
        factory.setUseGlobalResources(false);

        // 自定义连接池 - 所有参数从 application.yml 读取
        ConnectionProvider provider = ConnectionProvider.builder("rec-http-pool")
                .maxConnections(maxConnections)
                .pendingAcquireMaxCount(pendingAcquireMaxCount)
                .maxIdleTime(Duration.ofSeconds(maxIdleTimeSeconds))
                .maxLifeTime(Duration.ofMinutes(maxLifeTimeMinutes))
                .pendingAcquireTimeout(Duration.ofSeconds(pendingAcquireTimeoutSeconds))
                .evictInBackground(Duration.ofSeconds(evictInBackgroundSeconds))
                .build();

        factory.setConnectionProvider(provider);
        return factory;
    }

    /**
     * 创建支持负载均衡的 WebClient（使用 Reactor Netty）
     * 响应式 HTTP 客户端（WebFlux 风格）
     * 作用：支持 @HttpExchange 声明式调用 + 负载均衡
     */
    @Bean
    @LoadBalanced
    @ConditionalOnMissingBean
    public WebClient.Builder webClientBuilder(ReactorResourceFactory resourceFactory) {
        HttpClient httpClient = HttpClient.create(resourceFactory.getConnectionProvider())
                .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMillis)
                .responseTimeout(Duration.ofSeconds(readTimeoutSeconds))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new io.netty.handler.timeout.WriteTimeoutHandler(writeTimeoutSeconds))
                )
                .compress(true);

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }

    /**
     * 创建支持负载均衡的 RestClient（同步调用）
     * 同步 HTTP 客户端（Spring MVC 风格）
     */
    @Bean
    @LoadBalanced
    @ConditionalOnMissingBean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}