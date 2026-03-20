package com.kevin.demo.tracing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;

/**
 *
 * @author 王琪
 * @date 2026/3/20 09:13
 */
@Service
public class EchoClientService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CircuitBreakerFactory circuitBreakerFactory;
    private final EchoClient echoClient;

    public EchoClientService(CircuitBreakerFactory circuitBreakerFactory, EchoClient echoClient) {
        this.circuitBreakerFactory = circuitBreakerFactory;
        this.echoClient = echoClient;
    }

    public String echo(String name) {
        // 创建熔断器实例（name 对应 application.yml 配置）
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("echoService");

        // 执行调用，提供降级函数
        return circuitBreaker.run(
                () -> echoClient.echo(name),
                throwable -> {
                    logger.error("【熔断降级】echo用户失败，NAME: {}, 异常: {}",
                            name, throwable.getMessage());
                    return "name not found";
                }
        );
    }

    public String testError() {
        return circuitBreakerFactory.create("echoService")
                .run(
                        () -> echoClient.triggerError(),  // 这个调用会失败
                        throwable -> {
                            logger.error("【echo熔断降级】调用错误端点失败: {}", throwable.getMessage());
                            return "echo降级响应";
                        }
                );
    }
}
