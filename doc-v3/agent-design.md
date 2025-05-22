# Enhanced Mock Framework V3 MockAgent与Instrumentation实现细节

## 1. MockAgent作用与启动方式
- 负责注入Instrumentation，支持类重定义、字节码插桩。
- 启动方式：
  - JVM参数：`-javaagent:/path/to/mock-agent.jar`
  - 运行时动态attach：`MockAgent.attachToCurrentJVM()`
- 提供API供框架内部和外部动态attach。

## 2. Instrumentation用法与限制
- Instrumentation可在类加载前/后redefine类字节码。
- 适合mock构造器、final类/字段、record、sealed class等。
- 不能redefine JDK核心类（如java.lang.String），需结合代理/工厂。
- 需注意JVM安全策略与模块系统限制。

## 3. 构造器mock字节码插桩原理
- InstrumentationEngine在构造器体内插入mock拦截逻辑：
  1. 构造器体前判断ThreadLocal是否有mockInstance，有则抛出MockReturnException。
  2. 外部捕获异常，返回mockInstance。
- 伪代码：
```java
// 构造器插桩体内
if (MockContext.hasMockInstance()) {
    throw new MockReturnException(MockContext.getMockInstance());
}
// 原始构造器逻辑...
```
- 工厂/代理模式下，直接返回mockInstance，无需抛异常。

## 4. 类重定义流程与兼容性
- InstrumentationEngine通过`redefineClasses`方法替换目标类字节码。
- 支持运行时热替换，兼容JDK8-21。
- 需处理类加载器、模块边界、final字段等特殊场景。

## 5. 与Objenesis/Unsafe的协作机制
- 无法redefine时，降级为Objenesis/Unsafe分配对象，不调用构造函数。
- ObjenesisEngine结合动态代理/字节码增强实现方法拦截。
- MockFactory自动检测环境，选择最佳mock实现。

## 6. 典型代码片段与伪代码
```java
// MockAgent入口
public static void premain(String agentArgs, Instrumentation inst) {
    InstrumentationHolder.set(inst);
}

// InstrumentationEngine重定义类
inst.redefineClasses(new ClassDefinition(targetClass, modifiedBytecode));

// 构造器插桩伪代码
if (MockContext.hasMockInstance()) {
    throw new MockReturnException(MockContext.getMockInstance());
}
```

## 7. 未来JDK22+适配展望
- 适配record pattern matching、虚拟线程、sealed class等新特性。
- 关注JVM Project Valhalla、Loom等未来计划。

---

> 本文档持续更新MockAgent与Instrumentation实现细节。 