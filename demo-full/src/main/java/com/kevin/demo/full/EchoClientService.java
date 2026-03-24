package com.kevin.demo.full;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import org.springframework.stereotype.Service;

@Service
public class EchoClientService {

    private static final Logger log = LoggerFactory.getLogger(EchoClientService.class);

    private final EchoClient echoClient;
    private final CircuitBreaker circuitBreaker;

    public EchoClientService(EchoClient echoClient,
                             CircuitBreakerFactory<?,?> circuitBreakerFactory) {
        this.echoClient = echoClient;
        this.circuitBreaker = circuitBreakerFactory.create("echoClient");
    }

    public String echo(String name) {
        return circuitBreaker.run(
                () -> echoClient.echo(name),
                throwable -> echoFallback(name, throwable)
        );
    }

    public String testError(String msg) {
        return circuitBreaker.run(
                () -> echoClient.triggerError(msg),
                throwable -> echoErrorFallback(msg, throwable)
        );
    }

    public String safeEcho(String msg) {
        return circuitBreaker.run(
                () -> echoClient.triggerError(msg),
                throwable -> safeEchoFallback(msg, throwable)
        );
    }

    // -------------------- Fallbacks --------------------

    private String echoFallback(String name, Throwable t) {
        log.error("【熔断降级】echo失败, name={}", name, unwrap(t));
        return "name not found";
    }

    private String echoErrorFallback(String msg, Throwable t) {
        log.error("【熔断降级】调用错误端点失败, msg={}", msg, unwrap(t));
        return "echo降级响应";
    }

    private String safeEchoFallback(String msg, Throwable t) {
        log.warn("fallback: {}", msg, unwrap(t));
        return "fallback: " + msg;
    }

    private Throwable unwrap(Throwable t) {
        Throwable result = t;
        while (result instanceof NoFallbackAvailableException
                && result.getCause() != null) {
            result = result.getCause();
        }
        return result;
    }
}
