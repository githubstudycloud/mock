# Enhanced Mock Framework V3 MockFactory与对象创建机制

## 1. MockFactory作用与API
- 统一mock对象创建入口，屏蔽底层实现细节。
- 负责mock对象注册、查找、生命周期管理。
- 典型API：
```java
UserService mock = MockFactory.create(UserService.class);
MockFactory.register(mock, config -> config.stub("findById", 1L).thenReturn(...));
MockFactory.get(UserService.class);
MockFactory.clearAll();
```

## 2. InstrumentationEngine与ObjenesisEngine协作
- MockFactory自动检测Instrumentation可用性，优先用InstrumentationEngine，否则用ObjenesisEngine。
- InstrumentationEngine支持构造器mock、final类/字段、record、sealed class。
- ObjenesisEngine适合无Agent场景，结合代理/字节码增强实现方法拦截。

## 3. mock对象注册、查找、生命周期管理
- 支持按类型、名称、标签等多维度注册和查找mock对象。
- 支持mock对象的自动清理、重置、生命周期钩子。
- 便于集成到Spring等依赖注入框架。

## 4. 构造器mock与工厂/代理模式实现
- InstrumentationEngine下，构造器体内抛出MockReturnException，外部捕获后返回mockInstance。
- ObjenesisEngine下，直接分配对象内存，不调用构造函数。
- 工厂/代理模式下，所有mock对象通过MockFactory创建，避免直接new。

## 5. 典型用法与伪代码
```java
// 创建mock对象
UserService mock = MockFactory.create(UserService.class);

// 注册行为
MockFactory.register(mock, config -> config.stub("findById", 1L).thenReturn(...));

// 获取已注册mock
UserService mock2 = MockFactory.get(UserService.class);

// 清理所有mock
MockFactory.clearAll();
```

## 6. 兼容性与扩展性设计
- 支持多mock引擎切换，便于适配不同JDK和运行环境。
- 支持插件式mock行为、事件监听、mock链路追踪。
- 便于与Spring、Guice等依赖注入框架集成。

---

> 本文档持续更新MockFactory与对象创建机制设计。 