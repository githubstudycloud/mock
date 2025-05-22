# 集成与使用指南

## 1. 依赖引入

Maven:
```xml
<dependency>
  <groupId>com.mocktutorial</groupId>
  <artifactId>mock-core</artifactId>
  <version>4.x.x</version>
  <scope>test</scope>
</dependency>
```
Gradle:
```groovy
testImplementation 'com.mocktutorial:mock-core:4.x.x'
```

## 2. 基本用法

```java
import com.mocktutorial.core.v3.Mock;

// 创建mock对象
MyService mock = Mock.mock(MyService.class);
// 注册行为
Mock.when(mock, "hello", "world").thenReturn("hi, world");
// 验证调用
Mock.verify(mock).method("hello", "world").once();
```

## 3. 构造器/静态/私有方法mock

- 构造器mock：
  - 只能通过`ConstructorMocker.handleConstructorCall(...)`断言mock效果。
  - 示例：
    ```java
    ConstructorMocker.prepareForConstructorMocking(MyClass.class);
    MyClass mockInstance = new MyClass();
    mockInstance.setValue("mocked");
    ConstructorMocker.whenConstructor(MyClass.class, mockInstance);
    MyClass result = ConstructorMocker.handleConstructorCall(MyClass.class, new Object[0], new Class[0]);
    assertEquals("mocked", result.getValue());
    ```
- 静态方法mock：
  - 通过`StaticMocker.when/whenThrow/whenImplement`注册，`handleStaticMethodCall`断言。
- 私有方法mock：
  - 通过`PrivateMethodMocker.when/whenThrow/whenImplement`注册，`handlePrivateMethodCall`断言。

## 4. Spring集成

- 在Spring配置类中注册`SpringMockPostProcessor`：
  ```java
  @Bean
  public SpringMockPostProcessor springMockPostProcessor() {
      return new SpringMockPostProcessor();
  }
  ```
- 在需要mock的依赖字段上加`@MockField`注解：
  ```java
  @Autowired @MockField
  private MyService myService;
  ```
- 启动Spring容器后，mock对象会自动注入。

## 5. 在新项目中的最佳实践

- 推荐所有mock对象通过`Mock.mock`工厂方法创建，避免直接new。
- 行为注册、调用验证建议在每个测试用例独立配置，避免状态污染。
- 构造器/静态/私有方法mock建议只在必要场景使用，优先接口mock。
- Spring集成时，确保`@MockField`注解和`SpringMockPostProcessor`都已启用。

## 6. 常见问题与排查建议

- 构造器mock无法让new对象100%等于mockInstance？
  - 见[API说明](./api.md)和doc-history/v3-v2remark.md，Javassist方案下为已知限制。
- 静态/私有方法mock无效？
  - 检查是否已prepareForStaticMocking/prepareForPrivateMocking。
- Spring注入无效？
  - 检查`@MockField`和`SpringMockPostProcessor`是否生效。
- 兼容性问题？
  - 推荐JDK17/21，遇到JDK21+新特性问题可反馈issue。 