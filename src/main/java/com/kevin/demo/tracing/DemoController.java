package com.kevin.demo.tracing;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.annotation.Observed;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {
    private static final Logger log = LoggerFactory.getLogger(DemoController.class);

    private final Tracer tracer;
    private final ObservationRegistry observationRegistry;

    public DemoController(Tracer tracer, ObservationRegistry observationRegistry) {
        this.tracer = tracer;
        this.observationRegistry = observationRegistry;
    }

    @GetMapping("/hello")
    public String hello() {
        // 方式1: 使用 Tracer（传统方式）
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            log.info("TraceId: {}, SpanId: {}",
                    currentSpan.context().traceId(),
                    currentSpan.context().spanId());
        }

        // 方式2: 使用 Observation（推荐，Spring Boot 3+）
        return Observation.createNotStarted("hello.operation", observationRegistry)
                .observe(() -> {
                    log.info("Processing hello request");
                    return "Hello World!";
                });
    }

    // 方式3: 使用注解（最简洁）
    @GetMapping("/world")
    @Observed(name = "world.operation",
            contextualName = "getting-world",
            lowCardinalityKeyValues = {"method", "get"})
    public String world() {
        return "World!";
    }
}