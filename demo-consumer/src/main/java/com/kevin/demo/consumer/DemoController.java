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

    @GetMapping("/echo/{name}")
    @Observed(name = "echo.name",
            contextualName = "echo-name-sync",
            lowCardinalityKeyValues = {"client", "http-exchange", "mode", "sync"})
    public String echo(@PathVariable String name) {
        logger.info("start echo in demo controller");
        return echoClientService.echo(name);
    }

    /**
     * 强制触发异常（用于测试熔断）
     */
    @GetMapping("/trigger-error-echo/{msg}")
    public String triggerErrorEcho(@PathVariable String msg) {
        logger.info("start trigger echo error in demo controller");
        return echoClientService.testError(msg);
    }

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

    /**
     * 强制触发异常（用于测试熔断）
     */
    @GetMapping("/trigger-error-user")
    public String triggerErrorUser() {
        return userClientService.testError();
    }
}