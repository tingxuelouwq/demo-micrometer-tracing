package com.kevin.demo.sccb;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
public class DemoController {

    private final DemoService demoService;

    public DemoController(DemoService demoService) {
        this.demoService = demoService;
    }

    @GetMapping("/cb")
    public String testCircuitBreaker() {
        return demoService.callWithCircuitBreaker();
    }

    @GetMapping("/rl-tl-bh")
    public CompletableFuture<String> testRateTimeBulkhead() {
        return demoService.callWithRateAndTimeLimit();
    }
}
