# 架构设计

## 总体架构

- **API层**：统一暴露mock/when/verify等链式API，屏蔽底层实现。
- **Mock工厂与行为注册**：负责mock对象创建、行为注册与查找、调用记录。
- **Mock引擎**：
  - InstrumentationEngine：基于Instrumentation/ASM/ByteBuddy，支持构造器、final、静态、私有方法mock。
  - ObjenesisEngine：基于Objenesis/Unsafe，支持无构造函数实例化，结合JDK Proxy/cglib代理。
  - 代理/工厂模式：接口mock用JDK Proxy，类mock用cglib/ByteBuddy。
- **Spring/DI集成**：SpringMockPostProcessor自动注入@MockField标注的mock依赖。

## Mock引擎分层

- **InstrumentationEngine**：
  - 通过Java Agent或Attach API注入Instrumentation，支持类重定义和字节码插桩。
  - 可mock构造器、final类/方法、静态方法、私有方法。
  - 适合对JVM无侵入限制的场景。
- **ObjenesisEngine**：
  - 直接分配对象内存，不调用构造函数。
  - 结合JDK Proxy/cglib实现方法拦截。
  - 兼容JDK8-21+，适合大多数业务mock。
- **代理/工厂模式**：
  - 接口类型优先用JDK Proxy，类类型优先用cglib/ByteBuddy。
  - 支持行为注册、参数匹配、返回值/异常配置。

## API层与行为机制

- `Mock.mock(Class<T>)`：创建mock对象。
- `Mock.when(mock, method, args...)`：注册行为，支持thenReturn/thenThrow/thenImplement。
- `Mock.verify(mock).method(...).times(n)`：验证调用次数。
- 行为注册与查找分离，支持多mock实例、参数匹配、链式配置。

## Spring/DI集成

- 提供`@MockField`注解，配合`SpringMockPostProcessor`自动注入mock依赖。
- 支持Spring 5/6+，兼容JDK21。
- 日志与异常处理完善，便于调试。

## 兼容性与可扩展性

- 支持JDK8-21+，自动检测环境选择最佳mock引擎。
- API层与mock引擎解耦，便于后续扩展（如JDK22+新特性、更多DI框架集成）。
- 可插拔mock行为、验证、事件监听等扩展点。

## 设计原则与未来方向

- 保持API简洁、链式、易用，兼容主流mock习惯。
- 底层实现可切换，便于适配新JVM/新字节码技术。
- 未来可扩展：
  - JDK22+新特性支持（如defineHiddenClass等）
  - 更强的final/record/密封类mock能力
  - 多语言/多平台mock集成 