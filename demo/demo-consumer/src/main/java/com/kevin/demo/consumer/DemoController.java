package com.kevin.demo.consumer;

import com.kevin.demo.client.echo.EchoClientService;
import com.kevin.demo.client.user.User;
import com.kevin.demo.client.user.UserClientService;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class DemoController {
    private static final Logger logger = LoggerFactory.getLogger(DemoController.class);

    private final EchoClientService echoClientService;
    private final UserClientService userClientService;

    public DemoController(EchoClientService echoClientService, UserClientService userClientService) {
        this.echoClientService = echoClientService;
        this.userClientService = userClientService;
    }

    @GetMapping("/api/echo/{name}")
    @Observed(name = "echo.name",
            contextualName = "echo-name-sync",
            lowCardinalityKeyValues = {"client", "http-exchange", "mode", "sync"})
    public String echo(@PathVariable String name) {
        return echoClientService.echo(name);
    }

    @GetMapping("/api/echo/time/{name}")
    public String echoTime(@PathVariable String name) {
        return echoClientService.echoTime(name);
    }

    @GetMapping("/api/users/{id}")
    @Observed(name = "user.getById",
            contextualName = "fetching-user-by-id-sync",
            lowCardinalityKeyValues = {"client", "http-exchange", "mode", "sync"})
    public User getUser(@PathVariable Long id) {
        return userClientService.getUserByIdSync(id);
    }

    @GetMapping("/api/users")
    @Observed(name = "user.getAll",
            contextualName = "fetching-all-users-sync",
            lowCardinalityKeyValues = {"client", "http-exchange", "mode", "sync"})
    public List<User> getAllUsers() {
        return userClientService.getAllUsersSync();
    }

    @PostMapping("/api/users")
    @Observed(name = "user.create",
            contextualName = "creating-user",
            lowCardinalityKeyValues = {"client", "http-exchange", "mode", "sync"})
    public User createUserAsync(@RequestParam String name, @RequestParam String email) {
        return userClientService.createUser(name, email);
    }

    /**
     * 强制触发异常（用于测试熔断）
     */
    @GetMapping("/api/trigger-error-user")
    public String triggerErrorUser() {
        return userClientService.testError();
    }
}