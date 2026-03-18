package com.kevin.demo.tracing;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import java.util.List;

/**
 * 声明式 HTTP 客户端 - 调用本地 UserController
 * 使用 WebClient 实现，支持高并发（非阻塞 IO）
 */
@HttpExchange(url = "${internal.api.base-url}", accept = "application/json")
public interface UserClient {

    @GetExchange("/api/users/{id}")
    Mono<User> getUserById(@PathVariable Long id);

    @GetExchange("/api/users/{id}")
    User getUserByIdSync(@PathVariable Long id);

    @GetExchange("/api/users")
    Mono<List<User>> getAllUsers();

    @GetExchange("/api/users")
    List<User> getAllUsersSync();

    @PostExchange("/api/users")
    Mono<User> createUser(@RequestParam String name, @RequestParam String email);
}