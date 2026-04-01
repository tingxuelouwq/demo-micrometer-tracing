package com.kevin.demo.consumer;

import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class DemoController {
    private static final Logger logger = LoggerFactory.getLogger(DemoController.class);

    private final EchoClient echoClientService;

    public DemoController(EchoClient echoClientService) {
        this.echoClientService = echoClientService;
    }

    @GetMapping("/api/echo/{name}")
    @Observed(name = "echo.name",
            contextualName = "echo-name-sync",
            lowCardinalityKeyValues = {"client", "http-exchange", "mode", "sync"})
    public String echo(@PathVariable String name) {
        logger.info("user-consumer echo: " + name);
        return echoClientService.echo(name);
    }

    @GetMapping("/api/echo/time/{name}")
    public String echoTime(@PathVariable String name) {
        return echoClientService.echoTime(name);
    }

}