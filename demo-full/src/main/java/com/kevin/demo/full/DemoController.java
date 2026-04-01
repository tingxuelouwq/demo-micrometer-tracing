package com.kevin.demo.full;

import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
public class DemoController {
    private static final Logger log = LoggerFactory.getLogger(DemoController.class);

    private final EchoClient echoClient;

    public DemoController(EchoClient echoClient) {
        this.echoClient = echoClient;
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
        log.info("echo {} ", name);
        return echoClient.echo(name);
    }
}