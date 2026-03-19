package com.kevin.demo.tracing;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.Collections;
import java.util.List;

/**
 * 声明式 HTTP 客户端 - 调用本地 UserController
 * 集成 Resilience4j 熔断、重试、限流
 */
@HttpExchange(url = "/api/users")
public interface UserClient {

    /**
     * 根据ID获取用户
     * 配置：熔断 + 重试 + 限流
     */
    @GetExchange("/{id}")
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByIdFallback")
    @Retry(name = "userService")
    @RateLimiter(name = "userService")
    User getUserByIdSync(@PathVariable Long id);

    /**
     * 获取所有用户
     * 配置：熔断 + 限流
     */
    @GetExchange
    @CircuitBreaker(name = "userService", fallbackMethod = "getAllUsersFallback")
    @RateLimiter(name = "userService")
    List<User> getAllUsersSync();

    /**
     * 创建用户
     * 配置：熔断 + 重试
     */
    @PostExchange
    @CircuitBreaker(name = "userService", fallbackMethod = "createUserFallback")
    @Retry(name = "userService")
    User createUser(@RequestParam String name, @RequestParam String email);

    // ==================== Fallback 降级方法 ====================

    /**
     * getUserByIdSync 的降级方法
     * 方法签名：返回值和参数与主方法一致，最后一个参数可加上 Throwable
     */
    default User getUserByIdFallback(Long id, Throwable throwable) {
        System.err.println("获取用户降级触发，ID: " + id + "，异常: " + throwable.getMessage());

        // 返回默认用户或缓存数据
        return new User(id, "未知用户", "fallback@example.com");
    }

    /**
     * getAllUsersSync 的降级方法
     */
    default List<User> getAllUsersFallback(Throwable throwable) {
        System.err.println("获取用户列表降级触发，异常: " + throwable.getMessage());

        // 返回空列表或缓存数据
        return Collections.emptyList();
    }

    /**
     * createUser 的降级方法
     */
    default User createUserFallback(String name, String email, Throwable throwable) {
        System.err.println("创建用户降级触发，name: " + name + "，异常: " + throwable.getMessage());

        // 返回表示创建失败的临时对象
        return new User(-1L, name + "【创建失败】", email);
    }
}