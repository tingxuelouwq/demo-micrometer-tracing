package com.kevin.demo.consumer;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.decorators.Decorators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class UserClientService {

    private static final Logger log = LoggerFactory.getLogger(UserClientService.class);

    private final UserClient userClient;
    private final CircuitBreaker circuitBreaker;

    public UserClientService(UserClient userClient,
                             CircuitBreakerRegistry cbRegistry) {
        this.userClient = userClient;
        this.circuitBreaker = cbRegistry.circuitBreaker("demo-user-service");
    }

    // -------------------- Public API --------------------

    public User getUserByIdSync(Long id) {
        return Decorators.ofSupplier(() -> userClient.getUserByIdSync(id)).withCircuitBreaker(circuitBreaker)
                .withFallback(throwable -> getUserByIdFallback(id, throwable))
                .decorate()
                .get();
    }

    public List<User> getAllUsersSync() {
        return Decorators.ofSupplier(() -> userClient.getAllUsersSync()).withCircuitBreaker(circuitBreaker)
                .withFallback(throwable -> getAllUsersFallback(throwable))
                .decorate()
                .get();
    }

    public User createUser(String name, String email) {
        return Decorators.ofSupplier(() -> userClient.createUser(name, email)).withCircuitBreaker(circuitBreaker)
                .withFallback(throwable -> createUserFallback(name, email, throwable))
                .decorate()
                .get();
    }

    public String testError() {
        return Decorators.ofSupplier(() -> userClient.triggerError()).withCircuitBreaker(circuitBreaker)
                .withFallback(throwable -> userErrorFallback(throwable))
                .decorate()
                .get();
    }

    // -------------------- Fallbacks --------------------

    private User getUserByIdFallback(Long id, Throwable t) {
        log.error("【熔断降级】获取用户失败，ID={}, errMsg={}", id, unwrap(t).getMessage());
        return new User(id, "未知用户", "fallback@example.com");
    }

    private List<User> getAllUsersFallback(Throwable t) {
        log.error("【熔断降级】获取用户列表失败, errMsg={}", unwrap(t).getMessage());
        return Collections.emptyList();
    }

    private User createUserFallback(String name, String email, Throwable t) {
        log.error("【熔断降级】创建用户失败，name={}, errMsg={}", name, unwrap(t).getMessage());
        return new User(-1L, name + "【创建失败】", email);
    }

    private String userErrorFallback(Throwable t) {
        log.error("【user熔断降级】调用错误端点失败, errMsg={}", unwrap(t).getMessage());
        return "user降级响应, " + unwrap(t).getMessage();
    }

    private Throwable unwrap(Throwable t) {
        Throwable result = t;
        while (result instanceof NoFallbackAvailableException
                && result.getCause() != null) {
            result = result.getCause();
        }
        return result;
    }
}
