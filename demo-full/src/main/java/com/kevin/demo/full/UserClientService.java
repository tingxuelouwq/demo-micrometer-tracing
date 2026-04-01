//package com.kevin.demo.full;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
//import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
//import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
//import org.springframework.stereotype.Service;
//
//import java.util.Collections;
//import java.util.List;
//
//@Service
//public class UserClientService {
//
//    private static final Logger log = LoggerFactory.getLogger(UserClientService.class);
//
//    private final UserClient userClient;
//    private final CircuitBreaker circuitBreaker;
//
//    public UserClientService(UserClient userClient,
//                             CircuitBreakerFactory<?,?> circuitBreakerFactory) {
//        this.userClient = userClient;
//        this.circuitBreaker = circuitBreakerFactory.create("userClient");
//    }
//
//    // -------------------- Public API --------------------
//
//    public User getUserByIdSync(Long id) {
//        return circuitBreaker.run(
//                () -> userClient.getUserByIdSync(id),
//                throwable -> getUserByIdFallback(id, throwable)
//        );
//    }
//
//    public List<User> getAllUsersSync() {
//        return circuitBreaker.run(
//                () -> userClient.getAllUsersSync(),
//                this::getAllUsersFallback
//        );
//    }
//
//    public User createUser(String name, String email) {
//        return circuitBreaker.run(
//                () -> userClient.createUser(name, email),
//                throwable -> createUserFallback(name, email, throwable)
//        );
//    }
//
//    public String testError() {
//        return circuitBreaker.run(
//                () -> userClient.triggerError(),
//                this::userErrorFallback
//        );
//    }
//
//    // -------------------- Fallbacks --------------------
//
//    private User getUserByIdFallback(Long id, Throwable t) {
//        log.error("【熔断降级】获取用户失败，ID={}", id, unwrap(t));
//        return new User(id, "未知用户", "fallback@example.com");
//    }
//
//    private List<User> getAllUsersFallback(Throwable t) {
//        log.error("【熔断降级】获取用户列表失败", unwrap(t));
//        return Collections.emptyList();
//    }
//
//    private User createUserFallback(String name, String email, Throwable t) {
//        log.error("【熔断降级】创建用户失败，name={}", name, unwrap(t));
//        return new User(-1L, name + "【创建失败】", email);
//    }
//
//    private String userErrorFallback(Throwable t) {
//        log.error("【user熔断降级】调用错误端点失败", unwrap(t));
//        return "user降级响应";
//    }
//
//    private Throwable unwrap(Throwable t) {
//        Throwable result = t;
//        while (result instanceof NoFallbackAvailableException
//                && result.getCause() != null) {
//            result = result.getCause();
//        }
//        return result;
//    }
//}
