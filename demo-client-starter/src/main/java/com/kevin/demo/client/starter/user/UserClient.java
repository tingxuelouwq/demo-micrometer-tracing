//package com.kevin.demo.client.user;
//
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.service.annotation.GetExchange;
//import org.springframework.web.service.annotation.HttpExchange;
//import org.springframework.web.service.annotation.PostExchange;
//
//import java.util.List;
//
///**
// * 声明式 HTTP 客户端 - 调用本地 UserController
// */
//@HttpExchange
//public interface UserClient {
//
//    /**
//     * 根据ID获取用户
//     */
//    @GetExchange("/api/users/{id}")
//    User getUserByIdSync(@PathVariable Long id);
//
//    /**
//     * 获取所有用户
//     */
//    @GetExchange("/api/users")
//    List<User> getAllUsersSync();
//
//    /**
//     * 创建用户
//     */
//    @PostExchange("/api/users")
//    User createUser(@RequestParam String name, @RequestParam String email);
//
//    /**
//     * 专门用于测试熔断的错误端点 - 调用一个不存在的地址触发 404/500 错误
//     */
//    @GetExchange("/api/users/error")  // 这个端点不存在，会返回 404
//    String triggerError();
//}