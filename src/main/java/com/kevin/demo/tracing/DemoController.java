package com.kevin.demo.tracing;

import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {
    private static final Logger log = LoggerFactory.getLogger(DemoController.class);

    @GetMapping("/hello")
    @Observed(name = "hello.operation",
            contextualName = "getting-hello",
            lowCardinalityKeyValues = {"method", "get"})
    public String hello() {
        log.info("hello");
        return "hello!";
    }
}