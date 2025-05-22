# API说明

## 1. mock对象创建

```java
MyService mock = Mock.mock(MyService.class);
```
- 支持接口、普通类、final类。

## 2. 行为注册

```java
Mock.when(mock, "hello", "world").thenReturn("hi, world");
Mock.when(mock, "fail").thenThrow(new IllegalStateException("fail!"));
Mock.when(mock, "add", 1, 2).thenImplement(args -> (int)args[0] + (int)args[1] + 100);
```
- 支持参数匹配、返回值、异常、自定义实现。

## 3. 调用验证

```java
mock.hello("world");
Mock.verify(mock).method("hello", "world").once();
Mock.verify(mock).method("add", 1, 2).times(2);
```
- 支持调用次数校验，未命中抛出AssertionError。

## 4. 构造器/静态/私有方法mock

- 构造器mock：
  - 仅支持通过`ConstructorMocker.handleConstructorCall(...)`断言返回值，Javassist方案下new对象无法100%mock。
- 静态方法mock：
  - 通过`StaticMocker.when/whenThrow/whenImplement`注册，`handleStaticMethodCall`断言。
- 私有方法mock：
  - 通过`PrivateMethodMocker.when/whenThrow/whenImplement`注册，`handlePrivateMethodCall`断言。

## 5. Spring集成

- 使用`@MockField`注解在Spring Bean字段上，自动注入mock对象。
- 启用`SpringMockPostProcessor`。

```java
@Autowired @MockField
private MyService myService;
```

## 6. 代码示例

```java
// 创建mock
UserService userService = Mock.mock(UserService.class);
// 注册行为
Mock.when(userService, "findById", 1L).thenReturn(Optional.of(new User(1L, "Tom")));
Mock.when(userService, "findById", 999L).thenThrow(new RuntimeException("not found"));
// 验证
userService.findById(1L);
Mock.verify(userService).method("findById", 1L).once();
```

## 7. 兼容性说明
- 支持JDK8-21+，推荐JDK17/21。
- 支持JUnit5/4、TestNG等主流测试框架。
- Spring集成需Spring 5/6+。 