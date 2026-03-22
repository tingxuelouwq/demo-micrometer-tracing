package com.kevin.demo.full;

import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class DemoController {
    private static final Logger log = LoggerFactory.getLogger(DemoController.class);

    private final UserClientService userClientService;
    private final EchoClientService echoClientService;

    public DemoController(UserClientService userClientService, EchoClientService echoClientService) {
        this.userClientService = userClientService;
        this.echoClientService = echoClientService;
    }

    @GetMapping("/hello")
    @Observed(name = "hello.operation",
            contextualName = "getting-hello",
            lowCardinalityKeyValues = {"method", "get"})
    public String hello() {
        return "hello!";
    }

    // ==================== 同步调用 ====================

    @GetMapping("/users/{id}")
    @Observed(name = "user.getById",
            contextualName = "fetching-user-by-id-sync",
            lowCardinalityKeyValues = {"client", "http-exchange", "mode", "sync"})
    public User getUser(@PathVariable Long id) {
        return userClientService.getUserByIdSync(id);
    }

    @GetMapping("/users")
    @Observed(name = "user.getAll",
            contextualName = "fetching-all-users-sync",
            lowCardinalityKeyValues = {"client", "http-exchange", "mode", "sync"})
    public List<User> getAllUsers() {
        return userClientService.getAllUsersSync();
    }

    @PostMapping("/users")
    @Observed(name = "user.create",
            contextualName = "creating-user",
            lowCardinalityKeyValues = {"client", "http-exchange", "mode", "sync"})
    public User createUserAsync(@RequestParam String name, @RequestParam String email) {
        return userClientService.createUser(name, email);
    }

    @GetMapping("/echo/{name}")
    @Observed(name = "echo.name",
            contextualName = "echo-name-sync",
            lowCardinalityKeyValues = {"client", "http-exchange", "mode", "sync"})
    public String echo(@PathVariable String name) {
        return echoClientService.echo(name);
    }

    /**
     * 强制触发异常（用于测试熔断）
     */
    @GetMapping("/trigger-error-user")
    public String triggerErrorUser() {
        return userClientService.testError();
    }

    /**
     * 强制触发异常（用于测试熔断）
     */
    @GetMapping("/trigger-error-echo/{msg}")
    public String triggerErrorEcho(@PathVariable String msg) {
        return echoClientService.testError(msg);
    }

    @GetMapping("/safeEcho/{msg}")
    public String test(@PathVariable String msg) {
        return echoClientService.safeEcho(msg);
    }
}