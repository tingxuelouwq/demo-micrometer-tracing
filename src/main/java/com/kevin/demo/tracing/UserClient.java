package com.kevin.demo.tracing;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 声明式 HTTP 客户端 - 调用本地 UserController
 */
@HttpExchange(url = "/api/users")
public interface UserClient {

    @GetExchange("/{id}")
    Mono<User> getUserById(@PathVariable Long id);

    @GetExchange("/{id}")
    User getUserByIdSync(@PathVariable Long id);

    @GetExchange
    Mono<List<User>> getAllUsers();

    @GetExchange
    List<User> getAllUsersSync();

    @PostExchange
    Mono<User> createUser(@RequestParam String name, @RequestParam String email);
}