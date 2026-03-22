package com.kevin.demo.echo.client;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class EchoClientService {

    private static final Logger log = LoggerFactory.getLogger(EchoClientService.class);

    private final EchoClient echoClient;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;
    private final TimeLimiter timeLimiter;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    public EchoClientService(
            EchoClient echoClient,
            CircuitBreakerRegistry cbRegistry,
            RetryRegistry retryRegistry,
            TimeLimiterRegistry timeLimiterRegistry
    ) {
        this.echoClient = echoClient;
        this.circuitBreaker = cbRegistry.circuitBreaker("echoClient");
        this.retry = retryRegistry.retry("echoClient");
        this.timeLimiter = timeLimiterRegistry.timeLimiter("echoClient");
    }

    public String echo(String name) {
        return executeWithResilience(
                () -> echoClient.echo(name),
                t -> echoFallback(name, t)
        );
    }

    public String testError(String msg) {
        return executeWithResilience(
                () -> echoClient.triggerError(msg),
                t -> echoErrorFallback(msg, t)
        );
    }

    public String safeEcho(String msg) {
        return executeWithResilience(
                () -> echoClient.triggerError(msg),
                t -> echoClientFallback(msg, t)
        );
    }

    // -------------------- Fallbacks --------------------

    public String echoFallback(String name, Throwable t) {
        log.error("【熔断降级】echo用户失败，NAME: {}, EX: {}", name, t.getMessage());
        return "name not found";
    }

    public String echoErrorFallback(String msg, Throwable t) {
        log.error("【echo熔断降级】调用错误端点失败, MSG: {}, EX: {}", msg, t.getMessage());
        return "echo降级响应";
    }

    public String echoClientFallback(String msg, Throwable t) {
        log.info("fallback: {}", msg);
        return "fallback: " + msg;
    }

    // -------------------- Core Resilience Wrapper --------------------

    private <T> T executeWithResilience(Callable<T> action, java.util.function.Function<Throwable, T> fallback) {
        Callable<T> timeLimited = () ->
                timeLimiter.executeFutureSupplier(() -> executor.submit(action));
        Callable<T> retried = Retry.decorateCallable(retry, timeLimited);
        Callable<T> protectedCall = CircuitBreaker.decorateCallable(circuitBreaker, retried);

        try {
            return protectedCall.call();
        } catch (Throwable t) {
            return fallback.apply(t);
        }
    }
}
