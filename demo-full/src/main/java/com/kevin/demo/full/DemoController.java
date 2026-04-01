package com.kevin.demo.full;

import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class DemoController {
    private static final Logger log = LoggerFactory.getLogger(DemoController.class);

    private final UserClient userClientService;
    private final EchoClient echoClientService;

    public DemoController(UserClient userClientService, EchoClient echoClientService) {
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
        log.info("echo {} ", name);
        return echoClientService.echo(name);
    }

    @GetMapping("/echo/time/{name}")
    public String echoTime(@PathVariable String name) {
        return echoClientService.echoTime(name);
    }
}