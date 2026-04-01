package com.kevin.demo.consumer;

/**
 *
 * @author 王琪
 * @date 2026/4/1 11:31
 */
import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TestController {
    private static final Logger log = LoggerFactory.getLogger(TestController.class);

    @GetMapping("/test-mdc")
    public String testMdc() {
        // 打印所有 MDC 内容
        Map<String, String> mdcMap = MDC.getCopyOfContextMap();
        System.out.println("MDC content: " + mdcMap);

        // 尝试直接获取 OpenTelemetry 的键
        String traceId = MDC.get("trace_id");
        String spanId = MDC.get("span_id");

        System.out.println("trace_id from MDC: " + traceId);
        System.out.println("spanId from MDC: " + MDC.get("spanId")); // 尝试 Brave 的键名

        log.info("Test log message");

        return "MDC: " + mdcMap;
    }
}
