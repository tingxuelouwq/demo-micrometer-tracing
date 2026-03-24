package com.kevin.demo.full;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.decorators.Decorators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import org.springframework.stereotype.Service;

@Service
public class EchoClientService {

    private static final Logger log = LoggerFactory.getLogger(EchoClientService.class);

    private final EchoClient echoClient;
    private final CircuitBreaker circuitBreaker;

    public EchoClientService(EchoClient echoClient,
                             CircuitBreakerRegistry cbRegistry) {
        this.echoClient = echoClient;
        this.circuitBreaker = cbRegistry.circuitBreaker("echoClient");
    }

    public String echo(String name) {
        return Decorators.ofSupplier(() -> echoClient.echo(name)).withCircuitBreaker(circuitBreaker)
                .withFallback(throwable -> echoFallback(name, throwable))
                .decorate()
                .get();
    }

    public String echoTime(String name) {
        return Decorators.ofSupplier(() -> echoClient.echoTime(name)).withCircuitBreaker(circuitBreaker)
                .withFallback(throwable -> echoFallback(name, throwable))
                .decorate()
                .get();
    }

    // -------------------- Fallbacks --------------------
    private String echoFallback(String name, Throwable t) {
        return "【熔断降级】echo失败, name=" + name + ", errMsg={}" + unwrap(t).getMessage();
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
