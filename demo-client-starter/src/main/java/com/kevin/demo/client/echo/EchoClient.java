package com.kevin.demo.client.echo;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * 声明式 HTTP 客户端 - 调用本地 EchoController
 */
@HttpExchange
public interface EchoClient {

    @GetExchange("/echo/{name}")
    String echo(@PathVariable String name);
}