package com.kevin.demo.full;

import io.micrometer.observation.annotation.Observed;
import org.apache.catalina.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public String echo(@PathVariable String name) {
        log.info("echo {} ", name);
        return echoClientService.echo(name);
    }
}