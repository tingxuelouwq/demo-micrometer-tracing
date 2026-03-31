package com.kevin.demo.full;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/echo")
public class EchoController {

    private final Logger logger = LoggerFactory.getLogger(EchoController.class);

    @GetMapping("/{name}")
    String echo(@PathVariable String name) {
        logger.info("echo {} ", name);
        if (Math.random() > 0.5) {
            throw new RuntimeException("Simulated failure");
        }
        return "CircuitBreaker success, name=[" + name + "]";
    }
}
