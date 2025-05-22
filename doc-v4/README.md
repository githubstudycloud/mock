# Enhanced Mock Framework V4

## 文档体系与归档说明

- 历史文档（V1-V3开发历程、架构、API、mock-factory、agent设计、v2remark等）已全部归档至 doc-history/，便于追溯和查阅。
- 本目录（doc-v4）为正式发布文档，结构如下：
  - [开发历程归档](./history.md)：V1-V4演进与历史指向
  - [架构设计](./architecture.md)：V4分层、引擎、API、Spring集成、兼容性
  - [API说明](./api.md)：主API、mock/when/verify、构造器/静态/私有方法mock、Spring集成、代码示例
  - [集成与使用指南](./usage.md)：依赖、用法、最佳实践、常见问题排查
  - [高级特性与扩展](./advanced.md)：Instrumentation/Agent、Objenesis/Unsafe、字节码插桩、拦截器、未来方向
  - [常见问题FAQ](./faq.md)：构造器mock限制、静态/私有方法mock、Spring注入、JDK21+兼容性、贡献方式等
- 建议：新项目、团队集成、二次开发均优先查阅doc-v4，历史设计与演进请查doc-history/。

## 简介

本项目是面向JDK8-21+的高兼容性Mock测试框架，支持接口、类、构造器、静态方法、私有方法等多场景mock，具备Spring/DI集成、行为注册与验证、可插拔mock引擎等特性。

## 文档目录
- [开发历程归档](./history.md)
- [架构设计](./architecture.md)
- [API说明](./api.md)
- [集成与使用指南](./usage.md)
- [高级特性与扩展](./advanced.md)
- [常见问题FAQ](./faq.md)

## 适用范围与特性
- 支持JDK8-21及未来版本
- 支持接口、普通类、final类、构造器、静态方法、私有方法mock
- 支持Spring/DI自动注入mock
- 行为注册与调用验证分离，链式API
- 可插拔mock引擎（Instrumentation/Objenesis/代理）
- 兼容主流测试框架（JUnit5/4、TestNG等）

## 快速开始

```java
import com.mocktutorial.core.v3.Mock;

// 创建mock对象
MyService mock = Mock.mock(MyService.class);
// 注册行为
Mock.when(mock, "hello", "world").thenReturn("hi, world");
// 验证调用
Mock.verify(mock).method("hello", "world").once();
```

更多用法详见[集成与使用指南](./usage.md)。

## 版本与兼容性
- 当前版本：V4（建议用于新项目）
- 历史版本文档见doc-history/
- 兼容JDK8-21+，推荐JDK17/21

## 贡献与反馈
- 欢迎提交issue、PR或建议！
- 联系方式：见项目主页或issue区 

## 下一步V5优化方向与当前不足

### 当前不足
- 构造器mock在Javassist方案下无法让new对象100%等于mockInstance，仅能通过handleConstructorCall断言。
- Instrumentation/Agent方案尚未完全开源/集成，final/record/密封类mock能力有待提升。
- 静态/私有方法mock需prepare/handle调用，API体验略逊于主流mockito。
- Spring以外的DI框架（如Guice、Micronaut）集成尚未完善。
- JDK22+新特性（如defineHiddenClass、record pattern matching）支持待补充。

### V5版本优化计划
- 完善Instrumentation/Agent方案，支持真正的构造器mock、final/record/密封类mock。
- 提供更优雅的静态/私有方法mock API，提升易用性和一致性。
- 扩展多DI框架集成，支持Guice、Micronaut等。
- 适配JDK22+新特性，提升未来兼容性。
- 增强mock行为链路追踪、事件监听、全局mock能力。
- 丰富文档与示例，支持中英文双语文档。 