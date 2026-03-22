package com.kevin.demo.echo.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;

@Service
public class EchoClientService {

    private static final Logger log = LoggerFactory.getLogger(EchoClientService.class);

    private final EchoClient echoClient;
    private final CircuitBreaker circuitBreaker;

    public EchoClientService(EchoClient echoClient,
                             CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
        this.echoClient = echoClient;
        this.circuitBreaker = circuitBreakerFactory.create("demo-echo-service");
    }

    public String echo(String name) {
        return circuitBreaker.run(
                () -> echoClient.echo(name),
                t -> echoFallback(name, unwrap(t))
        );
    }

    public String testError(String msg) {
        return circuitBreaker.run(
                () -> echoClient.triggerError(msg),
                t -> echoErrorFallback(msg, unwrap(t))
        );
    }

    public String safeEcho(String msg) {
        return circuitBreaker.run(
                () -> echoClient.triggerError(msg),
                t -> safeEchoFallback(msg, unwrap(t))
        );
    }

    private String echoFallback(String name, Throwable t) {
        log.error("【熔断降级】echo失败, name={}, ex={}", name, t.toString());
        return "name not found";
    }

    private String echoErrorFallback(String msg, Throwable t) {
        log.error("【熔断降级】调用错误端点失败, msg={}, ex={}", msg, t.toString());
        return "echo降级响应";
    }

    private String safeEchoFallback(String msg, Throwable t) {
        log.warn("fallback: {}, ex={}", msg, t.toString());
        return "fallback: " + msg;
    }

    private Throwable unwrap(Throwable t) {
        while (t instanceof org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException
                && t.getCause() != null) {
            t = t.getCause();
        }
        return t;
    }
}
