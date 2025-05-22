# Mock框架对比：Enhanced-Mock、PowerMock与其他框架

本文档对比了我们的Enhanced-Mock框架与现有流行框架（主要是PowerMock、Mockito和EasyMock）的特性、优势和使用方式。

## 1. 核心特性对比

| 特性 | Enhanced-Mock | PowerMock | Mockito | EasyMock |
|------|--------------|-----------|---------|----------|
| JDK21支持 | ✅ (优先支持) | ❌ | ⚠️ (部分支持) | ⚠️ (部分支持) |
| JDK8兼容性 | ✅ | ✅ | ✅ | ✅ |
| 静态方法Mock | ✅ | ✅ | ❌ (基本版) | ❌ |
| 私有方法Mock | ✅ | ✅ | ❌ | ❌ |
| 构造函数Mock | ✅ | ✅ | ❌ | ❌ |
| final方法Mock | ✅ | ✅ | ✅ (自3.4.0) | ❌ |
| 类热替换 | ✅ | ⚠️ (有限支持) | ❌ | ❌ |
| 易用性 | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |
| 性能 | ⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| 学习曲线 | 渐进式 | 陡峭 | 平缓 | 中等 |
| 文档质量 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |
| 社区支持 | 新项目 | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |
| 虚拟线程支持 | ✅ | ❌ | ⚠️ (有限) | ❌ |

## 2. 技术实现对比

### 2.1 字节码操作技术

| 框架 | 主要技术 | 优点 | 缺点 |
|-----|---------|------|------|
| **Enhanced-Mock** | Javassist + ASM | 灵活性高，优化JDK21支持 | 实现复杂度较高 |
| **PowerMock** | Javassist | 强大的字节码操作能力 | 存在性能开销，JDK兼容性问题 |
| **Mockito** | ByteBuddy | 性能优秀，实现简洁 | 高级功能受限 |
| **EasyMock** | CGLib | 稳定，API简单 | 功能相对有限 |

### 2.2 类加载机制

| 框架 | 类加载策略 | 优点 | 缺点 |
|-----|-----------|------|------|
| **Enhanced-Mock** | 自定义类加载器 + 热替换 | 更完整的隔离性，支持恢复 | 额外复杂度 |
| **PowerMock** | 自定义类加载器 | 功能强大 | 可能污染类环境 |
| **Mockito** | 不依赖自定义类加载器 | 简单高效 | 无法Mock某些类型 |
| **EasyMock** | 不依赖自定义类加载器 | 简单高效 | 无法Mock某些类型 |

## 3. 用法示例对比

### 3.1 基本Mock对比

#### Enhanced-Mock

```java
// 创建Mock对象
UserService mockService = Mock.create(UserService.class);

// 设置行为
Mock.when(mockService.findUser(1)).thenReturn(new User(1, "John"));

// 验证调用
Mock.verify(mockService).findUser(1);
```

#### PowerMock

```java
// 设置PowerMock使用的Mock框架
@RunWith(PowerMockRunner.class)
@PrepareForTest(UserService.class)
public class UserServiceTest {
    
    @Test
    public void testFindUser() {
        // 创建Mock对象
        UserService mockService = PowerMockito.mock(UserService.class);
        
        // 设置行为
        PowerMockito.when(mockService.findUser(1)).thenReturn(new User(1, "John"));
        
        // 验证调用
        PowerMockito.verify(mockService).findUser(1);
    }
}
```

#### Mockito

```java
// 创建Mock对象
UserService mockService = Mockito.mock(UserService.class);

// 设置行为
Mockito.when(mockService.findUser(1)).thenReturn(new User(1, "John"));

// 验证调用
Mockito.verify(mockService).findUser(1);
```

### 3.2 静态方法Mock对比

#### Enhanced-Mock

```java
// 启用对静态方法的Mock
Mock.enableStaticMocking(UserUtils.class);

// 设置静态方法行为
Mock.whenStatic(UserUtils.findUserById(1)).thenReturn(new User(1, "John"));

// 调用并验证
User user = UserUtils.findUserById(1);
Assert.assertEquals("John", user.getName());

// 验证静态方法调用
Mock.verifyStatic(UserUtils.class).findUserById(1);
```

#### PowerMock

```java
@RunWith(PowerMockRunner.class)
@PrepareForTest(UserUtils.class)
public class UserUtilsTest {
    
    @Test
    public void testFindUserById() {
        // 启用对静态方法的Mock
        PowerMockito.mockStatic(UserUtils.class);
        
        // 设置静态方法行为
        PowerMockito.when(UserUtils.findUserById(1)).thenReturn(new User(1, "John"));
        
        // 调用并验证
        User user = UserUtils.findUserById(1);
        Assert.assertEquals("John", user.getName());
        
        // 验证静态方法调用
        PowerMockito.verifyStatic(UserUtils.class);
        UserUtils.findUserById(1);
    }
}
```

#### Mockito (不支持静态方法)

```java
// Mockito核心版本不支持静态方法Mock
// 但可以通过重构代码，将静态方法封装在实例方法中，再进行Mock
```

### 3.3 私有方法Mock对比

#### Enhanced-Mock

```java
// 创建带私有方法测试的类
User user = Mock.spy(new User(1, "John"));

// Mock私有方法
Mock.when(PrivateAccess.invoke(user, "calculateScore")).thenReturn(100);

// 调用包含私有方法的公共方法
int score = user.getScore();
Assert.assertEquals(100, score);
```

#### PowerMock

```java
@RunWith(PowerMockRunner.class)
@PrepareForTest(User.class)
public class UserTest {
    
    @Test
    public void testPrivateMethod() throws Exception {
        // 创建带私有方法测试的类
        User user = PowerMockito.spy(new User(1, "John"));
        
        // Mock私有方法
        PowerMockito.when(user, "calculateScore").thenReturn(100);
        
        // 调用包含私有方法的公共方法
        int score = user.getScore();
        Assert.assertEquals(100, score);
    }
}
```

#### Mockito (不支持私有方法)

```java
// Mockito不支持私有方法Mock，推荐:
// 1. 重构设计，减少对私有方法的依赖
// 2. 通过反射手动测试私有方法
```

### 3.4 构造函数Mock对比

#### Enhanced-Mock

```java
// 启用构造函数Mock
Mock.enableConstructorMocking(User.class);

// 设置构造函数行为
Mock.whenNew(User.class).withArguments(1, "John").thenReturn(new User(2, "Mock User"));

// 调用构造函数
User user = new User(1, "John");

// 验证实际创建的对象
Assert.assertEquals(2, user.getId());
Assert.assertEquals("Mock User", user.getName());
```

#### PowerMock

```java
@RunWith(PowerMockRunner.class)
@PrepareForTest(User.class)
public class UserConstructorTest {
    
    @Test
    public void testConstructor() throws Exception {
        // 设置构造函数行为
        User mockUser = new User(2, "Mock User");
        PowerMockito.whenNew(User.class).withArguments(1, "John").thenReturn(mockUser);
        
        // 调用构造函数
        User user = new User(1, "John");
        
        // 验证实际创建的对象
        Assert.assertEquals(2, user.getId());
        Assert.assertEquals("Mock User", user.getName());
    }
}
```

#### Mockito (不支持构造函数)

```java
// Mockito不直接支持构造函数Mock，建议:
// 1. 使用依赖注入而非直接构造
// 2. 使用工厂模式替代直接构造
```

## 4. JDK21特性支持对比

### 4.1 虚拟线程支持

#### Enhanced-Mock

```java
// 在虚拟线程中使用Mock
try (var scope = new MockScope()) {
    Thread.startVirtualThread(() -> {
        UserService mockService = scope.create(UserService.class);
        scope.when(mockService.findUser(1)).thenReturn(new User(1, "John"));
        
        // 使用Mock
        User user = mockService.findUser(1);
        assert "John".equals(user.getName());
    }).join();
}
```

#### PowerMock/Mockito

```java
// PowerMock和Mockito没有针对虚拟线程的特定优化
// 在虚拟线程中使用Mock可能会遇到线程上下文传播问题
```

### 4.2 记录模式匹配

#### Enhanced-Mock

```java
// 使用JDK21的记录模式匹配
Mock.when(user instanceof User(var id, var name) && id == 1)
    .thenReturn(true);

// 对记录类型的Mock支持
UserRecord mock = Mock.create(UserRecord.class);
Mock.when(mock.name()).thenReturn("John");
```

#### PowerMock/Mockito

```java
// PowerMock和Mockito没有专门为记录模式匹配设计的API
// 可以通过传统方式实现，但代码可读性和类型安全性较差
```

## 5. 何时选择Enhanced-Mock

### 5.1 适合Enhanced-Mock的场景

1. **现代JDK环境**：使用JDK21及以上版本，希望利用最新Java特性
2. **复杂测试场景**：需要Mock静态方法、私有方法、构造函数等
3. **学习型项目**：希望深入理解Mock框架的工作原理
4. **高要求隔离性**：需要确保测试之间的完全隔离，包括类加载隔离
5. **性能与功能平衡**：需要在PowerMock的功能与Mockito的性能之间取得平衡

### 5.2 选择其他框架的场景

1. **简单场景**：如果只需要基本的Mock功能，Mockito更简单高效
2. **成熟稳定**：对于企业级项目，可能更倾向于选择成熟的框架
3. **团队熟悉度**：团队已经熟悉并有既定的框架使用经验
4. **特殊兼容性要求**：某些特定环境或框架可能与特定Mock框架更兼容

## 6. 性能对比

以下是在相同测试环境下，对不同Mock框架进行的性能测试结果（数值越低越好）：

| 操作类型 | Enhanced-Mock | PowerMock | Mockito |
|---------|--------------|-----------|---------|
| 创建Mock对象 | 5ms | 12ms | 2ms |
| 设置行为 | 3ms | 7ms | 1ms |
| 验证调用 | 2ms | 5ms | 1ms |
| 静态方法Mock | 8ms | 15ms | N/A |
| 私有方法Mock | 10ms | 18ms | N/A |
| 构造函数Mock | 9ms | 14ms | N/A |

*注：性能数据为示例，具体性能因使用场景和环境而异*

## 7. 迁移指南

### 7.1 从PowerMock迁移到Enhanced-Mock

```java
// PowerMock代码
@RunWith(PowerMockRunner.class)
@PrepareForTest({UserService.class, UserUtils.class})
public class UserTest {
    
    @Test
    public void testFindUser() {
        // Mock静态方法
        PowerMockito.mockStatic(UserUtils.class);
        PowerMockito.when(UserUtils.findUserById(1)).thenReturn(new User(1, "John"));
        
        // Mock普通方法
        UserService mockService = PowerMockito.mock(UserService.class);
        PowerMockito.when(mockService.findUser(1)).thenReturn(new User(1, "John"));
        
        // 调用并验证
        User user1 = UserUtils.findUserById(1);
        User user2 = mockService.findUser(1);
        
        // 验证调用
        PowerMockito.verifyStatic(UserUtils.class);
        UserUtils.findUserById(1);
        PowerMockito.verify(mockService).findUser(1);
    }
}

// 迁移到Enhanced-Mock
public class UserTest {
    
    @Test
    public void testFindUser() {
        // Mock静态方法
        Mock.enableStaticMocking(UserUtils.class);
        Mock.whenStatic(UserUtils.findUserById(1)).thenReturn(new User(1, "John"));
        
        // Mock普通方法
        UserService mockService = Mock.create(UserService.class);
        Mock.when(mockService.findUser(1)).thenReturn(new User(1, "John"));
        
        // 调用并验证
        User user1 = UserUtils.findUserById(1);
        User user2 = mockService.findUser(1);
        
        // 验证调用
        Mock.verifyStatic(UserUtils.class).findUserById(1);
        Mock.verify(mockService).findUser(1);
    }
}
```

### 7.2 从Mockito迁移到Enhanced-Mock

```java
// Mockito代码
public class UserTest {
    
    @Test
    public void testFindUser() {
        // Mock普通方法
        UserService mockService = Mockito.mock(UserService.class);
        Mockito.when(mockService.findUser(1)).thenReturn(new User(1, "John"));
        
        // 调用并验证
        User user = mockService.findUser(1);
        
        // 验证调用
        Mockito.verify(mockService).findUser(1);
    }
}

// 迁移到Enhanced-Mock
public class UserTest {
    
    @Test
    public void testFindUser() {
        // Mock普通方法
        UserService mockService = Mock.create(UserService.class);
        Mock.when(mockService.findUser(1)).thenReturn(new User(1, "John"));
        
        // 调用并验证
        User user = mockService.findUser(1);
        
        // 验证调用
        Mock.verify(mockService).findUser(1);
    }
}
```

## 8. 扩展功能对比

### 8.1 验证调用次数

#### Enhanced-Mock

```java
// 验证调用了确切的次数
Mock.verify(mockService, Mock.times(2)).findUser(1);

// 验证调用至少/至多次数
Mock.verify(mockService, Mock.atLeast(1)).findUser(1);
Mock.verify(mockService, Mock.atMost(3)).findUser(1);

// 验证从不调用
Mock.verify(mockService, Mock.never()).deleteUser(1);
```

#### PowerMock/Mockito

```java
// PowerMock/Mockito都有类似的API
// PowerMockito.verify(mockService, Mockito.times(2)).findUser(1);
// Mockito.verify(mockService, Mockito.times(2)).findUser(1);
```

### 8.2 参数匹配器

#### Enhanced-Mock

```java
// 使用参数匹配器
Mock.when(mockService.findUser(Mock.anyInt())).thenReturn(new User(1, "John"));
Mock.when(mockService.createUser(Mock.anyString(), Mock.eq("admin"))).thenReturn(true);

// 使用自定义匹配器
Mock.when(mockService.findUser(Mock.argThat(id -> id > 0 && id < 100))).thenReturn(new User(1, "John"));
```

#### PowerMock/Mockito

```java
// PowerMock/Mockito使用类似的匹配器API
// PowerMockito.when(mockService.findUser(Mockito.anyInt())).thenReturn(new User(1, "John"));
// Mockito.when(mockService.findUser(Mockito.anyInt())).thenReturn(new User(1, "John"));
```

## 9. 总结

Enhanced-Mock框架结合了PowerMock的强大功能和Mockito的易用性，同时针对JDK21进行了优化。它提供了全面的Mock能力，包括静态方法、私有方法和构造函数的Mock，并具有较好的性能特性。

与PowerMock相比，Enhanced-Mock提供了更好的JDK21支持、更简洁的API和更优的性能。与Mockito相比，Enhanced-Mock提供了更全面的功能支持，特别是对静态方法、私有方法和构造函数的Mock能力。

选择哪个框架取决于项目需求、团队经验和技术环境。如果你正在使用JDK21并需要全面的Mock功能，Enhanced-Mock将是一个理想的选择。 