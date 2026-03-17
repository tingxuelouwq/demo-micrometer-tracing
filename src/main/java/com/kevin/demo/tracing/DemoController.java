package com.kevin.demo.tracing;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.annotation.Observed;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
public class DemoController {
    private static final Logger log = LoggerFactory.getLogger(DemoController.class);

    private final Tracer tracer;
    private final ObservationRegistry observationRegistry;
    private final MeterRegistry meterRegistry;  // Prometheus 指标注册表

    // 自定义计数器
    private final Counter helloCounter;
    private final Counter worldCounter;

    // 自定义计时器
    private final Timer helloTimer;

    public DemoController(Tracer tracer,
                          ObservationRegistry observationRegistry,
                          MeterRegistry meterRegistry) {
        this.tracer = tracer;
        this.observationRegistry = observationRegistry;
        this.meterRegistry = meterRegistry;

        // 初始化自定义计数器
        this.helloCounter = Counter.builder("demo.hello.calls")
                .description("Total number of hello calls")
                .tag("endpoint", "/hello")
                .register(meterRegistry);

        this.worldCounter = Counter.builder("demo.world.calls")
                .description("Total number of world calls")
                .tag("endpoint", "/world")
                .register(meterRegistry);

        // 初始化自定义计时器
        this.helloTimer = Timer.builder("demo.hello.duration")
                .description("Hello endpoint response time")
                .publishPercentiles(0.5, 0.95, 0.99)  // 发布百分位数据
                .register(meterRegistry);
    }

    @GetMapping("/hello")
    public String hello() {
        // 记录开始时间
        long start = System.nanoTime();

        // 增加自定义计数器
        helloCounter.increment();

        // 方式1: 使用 Tracer（传统方式）
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            log.info("TraceId: {}, SpanId: {}",
                    currentSpan.context().traceId(),
                    currentSpan.context().spanId());
        }

        // 方式2: 使用 Observation（推荐，Spring Boot 3+）
        String result = Observation.createNotStarted("hello.operation", observationRegistry)
                .observe(() -> {
                    log.info("Processing hello request");
                    // 模拟业务处理时间
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return "Hello World!";
                });

        // 记录耗时到自定义计时器
        helloTimer.record(System.nanoTime() - start, TimeUnit.NANOSECONDS);

        return result;
    }

    // 方式3: 使用注解（最简洁）
    @GetMapping("/world")
    @Observed(name = "world.operation",
            contextualName = "getting-world",
            lowCardinalityKeyValues = {"method", "get"})
    public String world() {
        worldCounter.increment();
        return "World!";
    }

    // ==================== 新增：更多自定义指标示例 ====================

    @GetMapping("/greet/{name}")
    public String greet(@PathVariable String name) throws Exception {
        // 使用 Gauge（通过 Gauge.builder 创建，但通常用于测量瞬时值的场景）
        // 这里演示 Counter + Timer 的组合使用

        Counter.builder("demo.greet.calls")
                .tag("name", name)
                .register(meterRegistry)
                .increment();

        return Timer.builder("demo.greet.duration")
                .tag("name", name)
                .register(meterRegistry)
                .recordCallable(() -> {
                    log.info("Greeting {}", name);
                    return "Hello, " + name + "!";
                });
    }

    @GetMapping("/metrics-info")
    public String metricsInfo() {
        return """
                Prometheus 指标端点: http://localhost:8080/actuator/prometheus

                可用端点:
                - /actuator/health      - 健康检查
                - /actuator/info        - 应用信息
                - /actuator/metrics     - 所有指标列表
                - /actuator/prometheus  - Prometheus 格式的指标数据

                自定义指标:
                - demo_hello_calls_total        - hello 接口调用次数
                - demo_hello_duration_seconds   - hello 接口响应时间
                - demo_world_calls_total        - world 接口调用次数
                - demo_greet_calls_total        - greet 接口调用次数（按 name 标签）
                - demo_greet_duration_seconds   - greet 接口响应时间（按 name 标签）

                自动采集的指标:
                - http_server_requests_seconds  - HTTP 请求指标（Spring Boot 自动采集）
                - jvm_*                         - JVM 相关指标
                - process_*                     - 进程相关指标
                - system_*                      - 系统相关指标
                """;
    }
}