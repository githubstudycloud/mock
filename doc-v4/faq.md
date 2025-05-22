# 常见问题FAQ

## Q1: 构造器mock为何new对象无法100%mock？
A: 目前Javassist方案只能在构造函数体内插入mock逻辑，无法阻止构造函数本身的字段赋值。建议只断言`handleConstructorCall`返回的对象。若需100%mock，需用Instrumentation redefine或工厂/代理方案，详见doc-history/v3-v2remark.md。

## Q2: 如何mock静态/私有方法？
A: 需先调用`prepareForStaticMocking/prepareForPrivateMocking`，再用`when/whenThrow/whenImplement`注册行为，通过`handleStaticMethodCall/handlePrivateMethodCall`断言。

## Q3: Spring注入mock无效怎么办？
A: 检查是否已在配置类注册`SpringMockPostProcessor`，并确保字段加了`@MockField`注解。建议用@Autowired+@MockField组合。

## Q4: JDK21+兼容性问题如何排查？
A: 推荐优先用Objenesis引擎，遇到Instrumentation/Unsafe受限时可反馈issue。部分JDK21+新特性需等待后续版本适配。

## Q5: 如何贡献代码或反馈bug？
A: 欢迎提交issue、PR或建议。请附详细环境、JDK版本、最小复现代码。

## Q6: 其他常见集成与用法疑问
- 推荐所有mock对象通过`Mock.mock`工厂方法创建。
- 行为注册、调用验证建议在每个测试用例独立配置。
- 遇到mock未生效时，优先检查prepare/when/verify调用顺序。 