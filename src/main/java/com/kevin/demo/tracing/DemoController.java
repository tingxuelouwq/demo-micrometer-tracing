package com.kevin.demo.tracing;

import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class DemoController {
    private static final Logger log = LoggerFactory.getLogger(DemoController.class);

    private final UserClient userClient;

    public DemoController(UserClient userClient) {
        this.userClient = userClient;
    }

    @GetMapping("/hello")
    @Observed(name = "hello.operation",
            contextualName = "getting-hello",
            lowCardinalityKeyValues = {"method", "get"})
    public String hello() {
        log.info("hello");
        return "hello!";
    }

    // ==================== 同步调用 ====================

    @GetMapping("/users/{id}")
    @Observed(name = "user.getById",
            contextualName = "fetching-user-by-id-sync",
            lowCardinalityKeyValues = {"client", "http-exchange", "mode", "sync"})
    public User getUser(@PathVariable Long id) {
        log.info("[SYNC] Fetching user with id: {}", id);
        return userClient.getUserByIdSync(id);
    }

    @GetMapping("/users")
    @Observed(name = "user.getAll",
            contextualName = "fetching-all-users-sync",
            lowCardinalityKeyValues = {"client", "http-exchange", "mode", "sync"})
    public List<User> getAllUsers() {
        log.info("[SYNC] Fetching all users");
        return userClient.getAllUsersSync();
    }

    // ==================== 异步调用（高并发） ====================

    @GetMapping("/users/async/{id}")
    @Observed(name = "user.getById.async",
            contextualName = "fetching-user-by-id-async",
            lowCardinalityKeyValues = {"client", "http-exchange", "mode", "async"})
    public Mono<User> getUserAsync(@PathVariable Long id) {
        log.info("[ASYNC] Fetching user with id: {}", id);
        return userClient.getUserById(id)
                .doOnNext(user -> log.info("[ASYNC] Got user: {}", user));
    }

    @GetMapping("/users/async")
    @Observed(name = "user.getAll.async",
            contextualName = "fetching-all-users-async",
            lowCardinalityKeyValues = {"client", "http-exchange", "mode", "async"})
    public Mono<List<User>> getAllUsersAsync() {
        log.info("[ASYNC] Fetching all users");
        return userClient.getAllUsers();
    }

    @PostMapping("/users/async")
    @Observed(name = "user.create.async",
            contextualName = "creating-user-async",
            lowCardinalityKeyValues = {"client", "http-exchange", "mode", "async"})
    public Mono<User> createUserAsync(@RequestParam String name, @RequestParam String email) {
        log.info("[ASYNC] Creating user: name={}, email={}", name, email);
        return userClient.createUser(name, email);
    }
}