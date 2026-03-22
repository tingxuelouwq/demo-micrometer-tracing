package com.kevin.demo.full;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * 声明式 HTTP 客户端 - 调用本地 EchoController
 */
@HttpExchange
public interface EchoClient {

    @GetExchange("/api/echo/{name}")
    String echo(@PathVariable String name);

    /**
     * 专门用于测试熔断的错误端点 - 调用一个不存在的地址触发 404/500 错误
     */
    @GetExchange("/api/echo/error")  // 这个端点不存在，会返回 404
    String triggerError();
}