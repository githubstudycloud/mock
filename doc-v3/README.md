本目录已归档为历史文档，最新文档请见doc-v4。

# Enhanced Mock Framework V3

## 1. 目标与愿景
- 支持真正的构造器mock，new出来的对象就是mockInstance或自定义实现。
- 兼容JDK8-21及未来版本，支持final类、final字段、record、sealed class等。
- 提供无侵入API，适配主流依赖注入框架（如Spring、Guice）。
- 适配JDK21+新特性，支持虚拟线程、高级类型等。

## 2. 推荐架构设计
- **核心mock引擎**：Instrumentation redefine（Java Agent）+ Objenesis/Unsafe双引擎，自动检测环境选择最佳方案。
- **API层**：统一用`Mock.mock(Class<T>)`、`Mock.when(...)`、`Mock.verify(...)`等API，屏蔽底层实现细节。
- **工厂与代理**：提供MockFactory工厂API，所有mock对象可通过工厂创建，支持动态代理和字节码增强。
- **Spring/DI集成**：提供starter和BeanPostProcessor，自动mock依赖。
- **JDK兼容性**：自动适配JDK8-21，未来可扩展JDK22+新特性。
- **可插拔拦截器**：支持自定义mock行为、验证、事件监听等。

## 3. 技术路线与实现要点
- Instrumentation redefine为主，Objenesis/Unsafe为辅，自动检测环境。
- 提供MockAgent，支持-premain和agentmain两种模式。
- 构造器mock采用ThreadLocal传递mockInstance，构造器体内抛出特殊异常，外部捕获后返回mockInstance。
- 支持final类/字段、record、sealed class。
- Spring集成：BeanPostProcessor自动mock依赖。
- 兼容虚拟线程、record等JDK21+新特性。

## 4. 未来扩展与社区计划
- 适配JDK22+新特性，支持record pattern matching、虚拟线程等。
- 提供可插拔mock行为、事件监听、mock链路追踪等高级特性。
- 完善文档与示例，推动社区生态。
- 与主流测试框架、CI/CD平台深度集成。

## 5. 参考文档与开源库
- [Mockito](https://github.com/mockito/mockito)（Instrumentation+Objenesis）
- [PowerMock](https://github.com/powermock/powermock)（Agent+ASM）
- [Byte Buddy](https://bytebuddy.net/#/)
- [Objenesis](https://objenesis.org/)

---

> 本目录将持续更新V3实现的设计文档、关键代码、集成示例和开发进度。 