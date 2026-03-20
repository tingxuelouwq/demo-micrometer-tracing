package com.kevin.demo.tracing;

import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class DemoController {
    private static final Logger log = LoggerFactory.getLogger(DemoController.class);

    private final UserClientService userClientService;

    public DemoController(UserClientService userClientService) {
        this.userClientService = userClientService;
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
        return userClientService.getUserByIdSync(id);
    }

    @GetMapping("/users")
    @Observed(name = "user.getAll",
            contextualName = "fetching-all-users-sync",
            lowCardinalityKeyValues = {"client", "http-exchange", "mode", "sync"})
    public List<User> getAllUsers() {
        log.info("[sync] Fetching all users");
        return userClientService.getAllUsersSync();
    }

    @PostMapping("/users")
    @Observed(name = "user.create",
            contextualName = "creating-user",
            lowCardinalityKeyValues = {"client", "http-exchange", "mode", "sync"})
    public User createUserAsync(@RequestParam String name, @RequestParam String email) {
        log.info("[ASYNC] Creating user: name={}, email={}", name, email);
        return userClientService.createUser(name, email);
    }

    /**
     * 强制触发异常（用于测试熔断）
     * 连续调用2次此端点，第3次应该触发熔断
     */
    @GetMapping("/trigger-error")
    public String triggerError() {
        return userClientService.testError();
    }
}