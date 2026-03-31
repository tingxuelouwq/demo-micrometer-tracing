
# Demo Microservices Framework

基于 Spring Boot 4.0.2 + Spring Cloud 2025.1.1 + JDK 21 的企业级微服务基础框架。

## 🎯 技术栈

| 组件 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 4.0.2 | 核心框架 (2026-01-22) |
| Spring Cloud | 2025.1.1 (Oakwood) | 微服务生态 |
| JDK | 21 | LTS 版本，虚拟线程支持 |
| Resilience4j | 2.3.0 | 熔断、限流、超时、并发控制 |

## 📁 工程结构

```
demo-microservice-framework/
├── demo-dependencies/                    # BOM 依赖管理
│   └── pom.xml                           # 统一版本定义
├── demo-microservice-common-starter/     # 微服务通用 Starter
│   ├── src/main/java/
│   └── src/main/resources/
│       └── META-INF/spring/
│           └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
├── demo-client-starter/                  # Client 服务通用 Starter
│   └── 集成生产者客户端 (HttpExchange)
└── demo/
│   ├── demo-gateway/                     # 网关服务
│   ├── demo-consumer/                    # 消费者服务
│   ├── demo-echo-producer/               # Echo 生产者服务
│   └── demo-user-producer/               # User 生产者服务
├── demo-sccb/                            # Spring Cloud Circuit Breaker 示例
├── demo-full/                            # 单体应用 (完整测试)
```

## 🏗️ 模块职责

### 1. demo-dependencies (BOM)
- **定位**: 版本管理中心
- **职责**: 
  - 统一定义所有依赖版本号
  - 管理 Spring Boot、Spring Cloud、Resilience4j 等核心版本
  - 业务应用继承此 POM

### 2. demo-microservice-common-starter
- **定位**: 微服务基础设施
- **功能**:
  - 统一日志配置 (Logback/Log4j2)
  - 统一异常处理
  - 统一响应封装
  - 健康检查端点
  - 基础监控指标 (Micrometer + Prometheus)
  - 分布式链路追踪

### 3. demo-client-starter
- **定位**: 服务调用端封装
- **功能**:
  - 集成 HTTPExchange 
  - 统一负载均衡配置
  - 统一超时与重试策略
  - **Resilience4j 熔断降级配置**
  - 生产者客户端接口定义集成

### 4. 业务服务
- **demo-gateway**: 网关，统一入口、路由转发、鉴权、限流
- **demo-consumer**: 调用生产者服务，演示熔断、限流场景
- **demo-echo-producer**: 基础回声服务，用于测试
- **demo-user-producer**: 用户服务，演示业务场景

### 5. demo-sccb
- Spring Cloud Circuit Breaker 5.0.1 演示工程 
- 展示底层熔断抽象使用

### 6. demo-full
- 单体应用形态
- 用于集成测试、性能基准测试

## ⚡ 核心特性: Resilience4j 韧性设计

所有熔断、限流、超时、并发控制均基于 **Resilience4j** 实现，替代已停更的 Hystrix。

### 支持的韧性模式

| 模式 | 注解/配置 | 用途 |
|------|----------|------|
| **Circuit Breaker** | `@CircuitBreaker` | 熔断降级，防止级联故障 |
| **Rate Limiter** | `@RateLimiter` | 限流保护，防止过载 |
| **Retry** | `@Retry` | 重试机制 (仅幂等操作) |
| **Bulkhead** | `@Bulkhead` | 并发隔离 (信号量/线程池) |
| **Time Limiter** | `@TimeLimiter` | 超时控制 |

### 最佳实践配置 

```yaml
# application.yml 示例配置
resilience4j:
  circuitbreaker:
    configs:
      default:
        failureRateThreshold: 50          # 失败率阈值
        slowCallRateThreshold: 80           # 慢调用阈值
        slowCallDurationThreshold: 1s       # 慢调用时间
        permittedNumberOfCallsInHalfOpenState: 10
        slidingWindowSize: 100
        minimumNumberOfCalls: 10            # 最小调用数，避免过早熔断
        waitDurationInOpenState: 10s        # 熔断持续时间
    instances:
      userService:
        baseConfig: default
  ratelimiter:
    configs:
      default:
        limitForPeriod: 100               # 周期内允许请求数
        limitRefreshPeriod: 1s            # 周期时长
        timeoutDuration: 0                 # 获取许可等待时间
  retry:
    configs:
      default:
        maxAttempts: 3
        waitDuration: 1s
        exponentialBackoffMultiplier: 2     # 指数退避 
        retryExceptions:
          - java.net.SocketTimeoutException
          - org.springframework.web.client.ResourceAccessException
        ignoreExceptions:
          - java.lang.IllegalArgumentException  # 业务异常不重试
  bulkhead:
    configs:
      default:
        maxConcurrentCalls: 50            # 信号量模式
        maxWaitDuration: 0
    instances:
      dbOperation:
        maxConcurrentCalls: 20            # 数据库操作隔离
```

### 使用示例

```java
@Service
public class EchoClientService {

    private static final Logger log = LoggerFactory.getLogger(EchoClientService.class);

    private final EchoClient echoClient;
    private final CircuitBreaker circuitBreaker;

    public EchoClientService(EchoClient echoClient,
                             CircuitBreakerRegistry cbRegistry) {
        this.echoClient = echoClient;
        this.circuitBreaker = cbRegistry.circuitBreaker("demo-echo-service");
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
        return "【熔断降级】echo失败, name=" + name + ", errMsg=" + unwrap(t).getMessage();
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
```

### 组合使用顺序 

Resilience4j 装饰器执行顺序（从内到外）：
```
Rate Limiter → Bulkhead → Circuit Breaker → Retry → Time Limiter → 业务逻辑
```

## 🚀 快速开始

### 环境要求
- JDK 21+
- Maven 3.9+
- Docker (可选，用于本地部署基础设施)

### 构建工程

```bash
cd demo-microservice-framework
mvn clean install -DskipTests
```

### 启动服务

```bash
# 启动基础设施 (Consul + Kafka + Zipkin)
docker-compose up -d 

# 启动网关
cd demo-gateway
mvn spring-boot:run

# 启动生产者
cd demo-echo-producer
mvn spring-boot:run

# 启动消费者
cd demo-consumer
mvn spring-boot:run
```

### 验证端点

| 服务 | 地址 | 说明 |
|------|------|------|
| Gateway | http://localhost:9070 | 统一入口 |
| Echo Producer | http://localhost:9051/actuator/health | 健康检查 |
| Consumer | http://localhost:9090/actuator/circuitbreakers | 熔断器状态 |

## 📊 监控与观测

### Actuator 端点

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,circuitbreakers,ratelimiters,retries,bulkheads,threaddump
  endpoint:
    health:
      show-details: always
    circuitbreakers:
      enabled: true
```

### 关键监控指标

- **Circuit Breaker 状态**: `/actuator/circuitbreakers`
- **Rate Limiter 统计**: `/actuator/ratelimiters`
- **Retry 指标**: `/actuator/retries`
- **Bulkhead 使用情况**: `/actuator/bulkheads`

## 🛠️ 开发规范

### 1. 版本管理
- **所有版本号必须在 `demo-dependencies` 中定义**

### 2. Starter 开发
- 自动配置类使用 `@AutoConfiguration`
- 配置属性使用 `@ConfigurationProperties`
- 条件注解使用 `@ConditionalOnMissingBean` 允许业务覆盖

### 3. 韧性设计原则 
- ✅ **必须提供降级方法 (Fallback)**
- ✅ **重试仅用于幂等操作**
- ✅ **使用指数退避策略**
- ✅ **区分业务异常与系统异常**
- ✅ **基于生产监控调整阈值**

### 4. 配置分层
```
优先级: 业务应用 > demo-client-starter > demo-microservice-common-starter > Spring Boot 默认
```

## 📚 相关文档

- [Spring Boot 4.0 文档](https://docs.spring.io/spring-boot/docs/4.0.2/reference/html/)
- [Spring Cloud 2025.1.1 发布说明](https://github.com/spring-cloud/spring-cloud-release/wiki/Spring-Cloud-2025.1-Release-Notes) 
- [Resilience4j 官方文档](https://resilience4j.readme.io/)

## 📝 更新日志

### 2026-03-31
- 初始版本
- 采用版本 Spring Boot 4.0.2 + Spring Cloud 2025.1.1
- 全面采用 Resilience4j 替代 Hystrix
- 支持 JDK 21 虚拟线程

---
