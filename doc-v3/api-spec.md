# Enhanced Mock Framework V3 API 说明

## 1. 主API用法

### 1.1 创建mock对象
```java
UserService mock = Mock.mock(UserService.class);
```

### 1.2 方法存根与行为定义
```java
Mock.when(mock, "findById", 1L).thenReturn(new User(1L, "Mocked"));
Mock.when(mock, "saveUser", any(User.class)).thenThrow(new RuntimeException("fail"));
```

### 1.3 方法调用验证
```java
Mock.verify(mock).once().findById(1L);
Mock.verify(mock).times(2).saveUser(any(User.class));
```

## 2. MockFactory工厂API
```java
UserService mock = MockFactory.create(UserService.class);
MockFactory.register(mock, config -> config.stub("findById", 1L).thenReturn(...));
```

## 3. MockAgent用法
- 启动JVM时加参数：
```
-javaagent:/path/to/mock-agent.jar
```
- 运行时动态attach：
```java
MockAgent.attachToCurrentJVM();
```

## 4. Spring/DI集成API
- Spring Boot自动配置：
```yaml
mock:
  enabled: true
  packages: com.example.service,com.example.repo
```
- BeanPostProcessor自动mock依赖：
```java
@Configuration
public class MockConfig {
    @Bean
    public static MockBeanPostProcessor mockBeanPostProcessor() {
        return new MockBeanPostProcessor();
    }
}
```

## 5. 兼容性与环境检测API
```java
if (Mock.isInstrumentationAvailable()) {
    // 使用InstrumentationEngine
} else if (Mock.isObjenesisAvailable()) {
    // 使用ObjenesisEngine
}
```

## 6. 未来API扩展点
- 支持mock链路追踪、事件监听、mock行为插件等。
- 适配JDK22+新特性API。

---

> 本文档持续更新V3 API设计与用法。 