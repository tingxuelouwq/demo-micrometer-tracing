package com.kevin.demo.consumer;

import com.kevin.demo.echo.client.EchoClientService;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {
    private static final Logger log = LoggerFactory.getLogger(DemoController.class);

    private final EchoClientService echoClientService;

    public DemoController(EchoClientService echoClientService) {
        this.echoClientService = echoClientService;
    }

    @GetMapping("/hello")
    @Observed(name = "hello.operation",
            contextualName = "getting-hello",
            lowCardinalityKeyValues = {"method", "get"})
    public String hello() {
        return "hello!";
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
    @GetMapping("/trigger-error-echo")
    public String triggerErrorEcho() {
        return echoClientService.testError();
    }
}