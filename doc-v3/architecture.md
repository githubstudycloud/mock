# Enhanced Mock Framework V3 架构设计

## 1. 架构总览

V3框架采用双引擎架构：Instrumentation redefine（Java Agent）+ Objenesis/Unsafe，自动检测环境选择最佳mock实现。API层统一对外，支持Spring等主流依赖注入框架，兼容JDK8-21及未来版本。

- **MockAgent**：Java Agent入口，负责注入Instrumentation，支持-premain和agentmain。
- **InstrumentationEngine**：基于Instrumentation redefine实现mock，支持构造器mock、final类/字段、record、sealed class等。
- **ObjenesisEngine**：基于Objenesis/Unsafe分配对象，不调用构造函数，适配无Agent场景。
- **MockFactory**：统一工厂API，负责mock对象的创建、注册与生命周期管理。
- **API层**：对外暴露`Mock.mock`、`Mock.when`、`Mock.verify`等API，屏蔽底层实现细节。
- **Spring集成**：提供BeanPostProcessor等自动mock依赖，支持Spring/Guice等主流DI框架。
- **拦截器体系**：支持自定义mock行为、验证、事件监听、mock链路追踪等。

## 2. 核心模块说明

### 2.1 MockAgent
- 通过-javaagent参数或Attach API注入Instrumentation。
- 负责类加载时或运行时redefine目标类字节码。
- 提供API供框架内部和外部动态attach。

### 2.2 InstrumentationEngine
- 负责基于Instrumentation的字节码增强。
- 支持构造器mock（通过ThreadLocal传递mockInstance，构造器体内抛出特殊异常，外部捕获后返回mockInstance）。
- 支持final类、final字段、record、sealed class。
- 兼容JDK8-21及未来版本。

### 2.3 ObjenesisEngine
- 负责在无Agent场景下分配对象（不调用构造函数）。
- 结合动态代理/字节码增强实现方法拦截。
- 适合普通业务mock、测试场景。

### 2.4 MockFactory
- 统一mock对象创建入口。
- 负责mock对象注册、查找、生命周期管理。
- 提供API：`MockFactory.create(Class<T>)`、`MockFactory.register(...)`等。

### 2.5 API层
- 对外暴露统一API：`Mock.mock`、`Mock.when`、`Mock.verify`等。
- 屏蔽底层mock实现细节，便于后续扩展和升级。

### 2.6 Spring/DI集成
- 提供Spring BeanPostProcessor，自动mock依赖Bean。
- 支持Guice等主流依赖注入框架。
- 兼容Spring Boot自动配置。

### 2.7 拦截器体系
- 支持自定义mock行为、验证、事件监听、mock链路追踪。
- 可插拔设计，便于扩展。

## 3. 组件间调用流程

### 3.1 mock对象创建流程
1. 用户调用`Mock.mock(UserService.class)`。
2. MockFactory判断当前环境，优先选择InstrumentationEngine，否则用ObjenesisEngine。
3. InstrumentationEngine通过Instrumentation redefine目标类，插桩构造器和方法。
4. ObjenesisEngine通过Unsafe/Objenesis分配对象，结合代理实现方法拦截。
5. 返回mock对象。

### 3.2 构造器mock流程
1. InstrumentationEngine在构造器体内插入mock拦截逻辑。
2. 若命中mock配置，通过ThreadLocal传递mockInstance，构造器体内抛出MockReturnException。
3. 外部捕获异常，返回mockInstance。

### 3.3 方法拦截流程
1. InstrumentationEngine/ObjenesisEngine插桩或代理目标方法。
2. 方法调用时进入mock拦截器，返回stub/spy/verify等mock行为。

### 3.4 依赖注入mock流程
1. Spring BeanPostProcessor扫描Bean定义。
2. 自动将目标Bean替换为mock对象。
3. 支持mock配置、行为定义、验证等。

## 4. 兼容性与可扩展性设计
- 自动检测JDK版本、Agent/Objenesis可用性，选择最佳mock实现。
- API层与mock引擎解耦，便于后续扩展。
- 支持插件式拦截器、行为扩展、mock链路追踪。

## 5. 未来JDK22+适配展望
- 适配record pattern matching、虚拟线程、sealed class等新特性。
- 支持JVM Project Valhalla、Loom等未来计划。

---

> 本文档持续更新V3架构设计与实现细节。 