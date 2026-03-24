package com.kevin.demo.sccb;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;
import java.util.function.Supplier;

@Service
public class DemoService {

    private final CircuitBreakerRegistry cbRegistry;
    private final RateLimiterRegistry rateLimiterRegistry;
    private final TimeLimiterRegistry timeLimiterRegistry;
    private final BulkheadRegistry bulkheadRegistry;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public DemoService(CircuitBreakerRegistry cbRegistry, RateLimiterRegistry rlReg, TimeLimiterRegistry tlReg,
                       BulkheadRegistry bhReg) {
        this.cbRegistry = cbRegistry;
        this.rateLimiterRegistry = rlReg;
        this.timeLimiterRegistry = tlReg;
        this.bulkheadRegistry = bhReg;
    }

    /**
     * CircuitBreaker only (SCCB abstraction).
     * Here’s the flow:
     *
     * Normal path:
     * If Math.random() returns ≤ 0.7, the simulated call succeeds.
     * The breaker returns "CircuitBreaker success".
     *
     * Failure path:
     * If Math.random() returns > 0.5, an exception is thrown.
     * The breaker catches it and invokes the fallback lambda.
     * You get "CircuitBreaker fallback" in the response.
     *
     * Breaker state transitions:
     * After enough failures (≥ 50% of last 10 calls in your config), the breaker moves to OPEN.
     * While OPEN, even successful calls are short‑circuited → fallback is returned immediately.
     * After 10s, it goes HALF‑OPEN and tests again.
     * If the test succeeds, it closes; if it fails, it reopens.
     */
    public String callWithCircuitBreaker() {
        CircuitBreaker circuitBreaker = cbRegistry.circuitBreaker("cbService");
        return Decorators.ofSupplier(() -> {
                    if (Math.random() > 0.5) {
                        throw new RuntimeException("Simulated failure");
                    }
                    return "CircuitBreaker success";
                })
                .withCircuitBreaker(circuitBreaker)
                .withFallback(throwable -> "Fallback due to CircuitBreaker or other error: " + throwable.getMessage())
                .decorate()
                .get();
    }

    /**
     * @TimeLimiter 注解在 Spring Boot 中的实现机制是：
     * 方法返回 CompletableFuture 后，AOP 拦截器会包装这个 Future
     * 设置一个定时任务，在 timeout-duration（2秒）后如果 Future 没完成，就触发超时
     * 但是，cancel-running-future: true 只是调用 CompletableFuture.cancel(true)，这只会发送中断信号
     * 关键 bug：你的 supplyAsync 使用了 默认的 ForkJoinPool，其中的线程是 守护线程，而且 Thread.sleep() 对中断的反应是抛出 InterruptedException，你捕获后又抛出了 RuntimeException，但这个异常被 CompletableFuture 内部捕获并转换为异常完成。
     * 然而，由于 TimeLimiter 的超时机制可能没有正确处理这种异常情况，导致最终返回了原始结果。
     * 另外，注解方式确实不工作，建议使用编程式 API，它更可控且易于调试。
     */
    public CompletableFuture<String> callWithRateAndTimeLimit() {
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter("rlService");
        TimeLimiter timeLimiter = timeLimiterRegistry.timeLimiter("tlService");
        Bulkhead bulkhead = bulkheadRegistry.bulkhead("bhService");

        // Supplier simulating a slow call
        Supplier<CompletionStage<String>> supplier = () ->
                CompletableFuture.supplyAsync(() -> {
                    try {
                        Thread.sleep(3000); // deliberately longer than 2s timeout
                        return "Unified success";
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new CompletionException(e);
                    }
                }, executor);

        // Decorate with RL + BH
        Supplier<CompletionStage<String>> rateAndBulkheadDecorated =
                Decorators.ofCompletionStage(supplier)
                        .withRateLimiter(rateLimiter)
                        .withBulkhead(bulkhead)
                        .decorate();

        // Apply TimeLimiter last
        return timeLimiter.executeCompletionStage(scheduler, rateAndBulkheadDecorated)
                .toCompletableFuture()
                .exceptionally(this::handleFallback);
    }

    private String handleFallback(Throwable throwable) {
        Throwable cause = throwable instanceof CompletionException ?
                throwable.getCause() : throwable;

        if (cause instanceof TimeoutException) {
            return "Fallback due to TimeLimiter (timeout exceeded)";
        } else if (cause instanceof RequestNotPermitted) {
            return "Fallback due to RateLimiter (too many requests)";
        } else if (cause instanceof BulkheadFullException) {
            return "Fallback due to Bulkhead (too many concurrent calls)";
        }
        return "Fallback due to unexpected error: " + cause.getMessage();
    }
}