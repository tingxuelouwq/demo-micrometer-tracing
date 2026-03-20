package com.kevin.demo.tracing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author 王琪
 * @date 2026/3/20 09:13
 */
@Service
public class UserClientService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CircuitBreakerFactory circuitBreakerFactory;
    private final UserClient userClient;

    public UserClientService(CircuitBreakerFactory circuitBreakerFactory, UserClient userClient) {
        this.circuitBreakerFactory = circuitBreakerFactory;
        this.userClient = userClient;
    }

    /**
     * 获取用户 - 带熔断保护
     */
    public User getUserByIdSync(Long id) {
        // 创建熔断器实例（name 对应 application.yml 配置）
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("userService");

        // 执行调用，提供降级函数
        return circuitBreaker.run(
                () -> userClient.getUserByIdSync(id),
                throwable -> {
                    logger.error("【熔断降级】获取用户失败，ID: {}, 异常: {}",
                            id, throwable.getMessage());
                    return new User(id, "未知用户", "fallback@example.com");
                }
        );
    }

    /**
     * 获取所有用户 - 带熔断保护
     */
    public List<User> getAllUsersSync() {
        return circuitBreakerFactory.create("userService")
                .run(
                        userClient::getAllUsersSync,
                        throwable -> {
                            logger.error("【熔断降级】获取用户列表失败: {}", throwable.getMessage());
                            return Collections.emptyList();
                        }
                );
    }

    /**
     * 创建用户 - 带熔断保护
     */
    public User createUser(String name, String email) {
        return circuitBreakerFactory.create("userService")
                .run(
                        () -> userClient.createUser(name, email),
                        throwable -> {
                            logger.error("【熔断降级】创建用户失败，name: {}, 异常: {}",
                                    name, throwable.getMessage());
                            return new User(-1L, name + "【创建失败】", email);
                        }
                );
    }

    public String testError() {
        return circuitBreakerFactory.create("userService")
                .run(
                        () -> userClient.triggerError(),  // 这个调用会失败
                        throwable -> {
                            logger.error("【熔断降级】调用错误端点失败: {}", throwable.getMessage());
                            return "降级响应";
                        }
                );
    }
}
