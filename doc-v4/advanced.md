# 高级特性与扩展

## 1. Instrumentation/Agent高级mock
- 支持通过Java Agent注入Instrumentation，实现类重定义和字节码插桩。
- 可mock构造器、final类/方法、静态方法、私有方法。
- 适合对JVM有控制权的集成测试、框架测试等场景。
- 需以`-javaagent`参数启动，或用Attach API动态加载。

## 2. Objenesis/Unsafe能力
- 通过Objenesis/Unsafe可直接分配对象内存，不调用构造函数。
- 适合无侵入、兼容性要求高的业务mock。
- JDK21+下部分Unsafe API受限，推荐优先Objenesis。

## 3. 字节码插桩与自定义拦截器
- 支持基于ASM/ByteBuddy/Javassist的字节码插桩。
- 可扩展自定义mock行为、调用监听、事件处理等拦截器。
- 便于实现复杂mock场景（如AOP、全局mock、调用链追踪等）。

## 4. Spring/DI深度集成
- 支持Spring 5/6+，自动注入mock依赖。
- 可扩展支持Guice、Micronaut等其他DI框架。
- 支持多容器/多上下文mock隔离。

## 5. 未来扩展方向
- JDK22+新特性支持（如defineHiddenClass、record/密封类mock）。
- 多语言mock（如Kotlin/Scala/Groovy等JVM语言）。
- 多平台（如GraalVM、Android等）兼容。
- 更强的final/record/密封类mock能力。

## 6. 参考实现与对比
- [Mockito](https://github.com/mockito/mockito)：Instrumentation+Objenesis，业界主流mock框架。
- [PowerMock](https://github.com/powermock/powermock)：Agent+ASM，支持静态/构造器mock。
- [Byte Buddy](https://bytebuddy.net/#/)：强大的字节码生成与插桩库。
- 本框架兼容主流mock理念，API更简洁，JDK21+适配更好，易于二次开发和集成。 