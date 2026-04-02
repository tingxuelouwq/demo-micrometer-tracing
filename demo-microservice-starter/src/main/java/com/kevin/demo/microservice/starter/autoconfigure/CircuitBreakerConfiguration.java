package com.kevin.demo.microservice.starter.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.circuitbreaker.retry.FrameworkRetryCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.retry.FrameworkRetryConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.retry.RetryPolicy;

import java.time.Duration;

/**
 * Framework Retry Circuit Breaker 默认配置
 * <p>
 * 提供全局默认的熔断器配置，包括重试策略、熔断超时等参数。
 * 应用可以通过自定义 Customizer Bean 覆盖默认配置。
 * </p>
 */
@AutoConfiguration
@ConditionalOnClass(FrameworkRetryCircuitBreakerFactory.class)
public class CircuitBreakerConfiguration {

    /**
     * 默认熔断器配置
     * <p>
     * 重试策略：使用 Spring Framework 7 默认重试策略，如果调用抛出异常，会根据 RetryPolicy 自动重试。
     * <p>
     * 熔断策略：
     * <ul>
     *   <li>当失败次数达到阈值，熔断器进入 打开状态。</li>
     *   <li>在打开状态下，所有调用直接走 fallback，不再尝试。</li>
     *   <li>openTimeout(20s)：熔断器保持打开 20 秒后进入半开状态。</li>
     *   <li>resetTimeout(5s)：在半开状态下，如果 5 秒内没有失败，会自动恢复为关闭状态。</li>
     * </ul>
     * <p>
     * 注意：如需自定义重试次数、超时时间、异常类型等，请在应用中定义自己的 Customizer Bean，例如：
     * <pre>{@code
     * @Configuration
     * public class EchoClientCircuitBreakerConfig {
     *
     *     @Bean
     *     public Customizer<FrameworkRetryCircuitBreakerFactory> echoClientCircuitBreakerCustomizer() {
     *         return factory -> factory.configure("echoClient", id ->
     *                 new FrameworkRetryConfigBuilder(id)
     *                         .retryPolicy(RetryPolicy.withMaxRetries(5))
     *                         .openTimeout(Duration.ofSeconds(10))
     *                         .resetTimeout(Duration.ofSeconds(3))
     *                         .build()
     *         );
     *     }
     * }
     * }</pre>
     */
    @Bean
    public Customizer<FrameworkRetryCircuitBreakerFactory> defaultCircuitBreakerCustomizer() {
        return factory -> factory.configureDefault(id ->
                new FrameworkRetryConfigBuilder(id)
                        .retryPolicy(RetryPolicy.withDefaults())
                        .openTimeout(Duration.ofSeconds(20))
                        .resetTimeout(Duration.ofSeconds(5))
                        .build()
        );
    }
}


