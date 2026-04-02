
# Demo Microservices Framework

基于 Spring Boot 4.0.2 + Spring Cloud 2025.1.1 + JDK 21 的企业级微服务基础框架。

## 🎯 技术栈

| 组件           | 版本                 | 说明 |
|--------------|--------------------|------|
| Spring Boot  | 4.0.2              | 核心框架 (2026-01-22) |
| Spring Cloud | 2025.1.1 (Oakwood) | 微服务生态 |
| JDK          | 21                 | LTS 版本，虚拟线程支持 |
| Spring Retry | 5.0.1              | 熔断、超时 |

## 📁 工程结构

```
demo-microservice-framework/
├── demo-dependencies/                    # BOM 依赖管理
│   └── pom.xml                           # 统一版本定义
├── demo-microservice-starter/            # 微服务通用 Starter
│   ├── src/main/java/
│   └── src/main/resources/
│       └── META-INF/spring/
│           └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
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
  - 管理 Spring Boot、Spring Cloud 等核心版本

### 2. demo-microservice-starter
- **定位**: 微服务基础设施
- **职责**：  
  - 统一封装公共配置
  - 业务应用继承此 POM
- **功能**:
  - 统一日志配置 (Logback/Log4j2)
  - 统一异常处理
  - 统一响应封装
  - 健康检查端点
  - 基础监控指标 (Micrometer + Prometheus)
  - 分布式链路追踪

### 3. 业务服务
- **demo-gateway**: 网关，统一入口、路由转发、鉴权、限流
- **demo-consumer**: 调用生产者服务，演示熔断、限流场景
- **demo-echo-producer**: 基础回声服务，用于测试
- **demo-user-producer**: 用户服务，演示业务场景

### 4 demo-sccb
- Spring Cloud Circuit Breaker 5.0.1 演示工程 
- 展示底层熔断抽象使用

### 5. demo-full
- 单体应用形态
- 用于集成测试、性能基准测试

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

## 🛠️ 开发规范

### 1. 版本管理
- **所有版本号必须在 `demo-dependencies` 中定义**

### 2. Starter 开发
- 自动配置类使用 `@AutoConfiguration`
- 配置属性使用 `@ConfigurationProperties`
- 条件注解使用 `@ConditionalOnMissingBean` 允许业务覆盖

### 3. 配置分层
```
优先级: 业务应用 > demo-microservice-starter > Spring Boot 默认
```

## 📚 相关文档

- [Spring Boot 4.0 文档](https://docs.spring.io/spring-boot/docs/4.0.2/reference/html/)
- [Spring Cloud 2025.1.1 发布说明](https://github.com/spring-cloud/spring-cloud-release/wiki/Spring-Cloud-2025.1-Release-Notes) 
- [Resilience4j 官方文档](https://resilience4j.readme.io/)

## 📝 更新日志

### 2026-03-31
- 采用版本 Spring Boot 4.0.2 + Spring Cloud 2025.1.1
- 支持 JDK 21 虚拟线程
---
