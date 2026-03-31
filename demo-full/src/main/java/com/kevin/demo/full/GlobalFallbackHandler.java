package com.kevin.demo.full;

import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.concurrent.TimeoutException;

@ControllerAdvice
public class GlobalFallbackHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAll(Exception ex) {
        Throwable rootCause = getRootCause(ex);

        String reason = rootCause.getMessage();
        if (reason == null || reason.isBlank()) {
            reason = rootCause.getClass().getSimpleName();
        }

        if (rootCause instanceof CallNotPermittedException) {
            return ResponseEntity.ok("【熔断降级】服务不可用，原因: " + reason);
        } else if (rootCause instanceof TimeoutException) {
            return ResponseEntity.ok("【超时降级】请求超时，原因: " + reason);
        } else if (rootCause instanceof RequestNotPermitted) {
            return ResponseEntity.ok("【限流降级】请求过多，原因: " + reason);
        } else if (rootCause instanceof BulkheadFullException) {
            return ResponseEntity.ok("【舱壁降级】并发过多，原因: " + reason);
        } else {
            return ResponseEntity.ok("【全局异常降级】系统繁忙，请稍后再试。根因: " + reason);
        }
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable.getCause();
        if (cause != null && cause != throwable) {
            return getRootCause(cause);
        }
        return throwable;
    }
}
