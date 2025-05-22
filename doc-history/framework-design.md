# 增强型Mock框架设计文档

## 1. 总体设计思路

本框架旨在创建一个类似于PowerMock的增强型Mock框架，优先支持JDK21，同时保持向下兼容至JDK8。通过使用Javassist进行字节码操作，实现静态方法、私有方法和构造函数的Mock功能，并支持类热替换。

### 1.1 核心设计理念

1. **优先支持JDK21**: 充分利用JDK21的新特性如虚拟线程、记录模式匹配等
2. **向下兼容**: 保持与JDK8+的兼容性，使用多版本JAR技术
3. **易学易用**: 提供渐进式学习路径，从基础到高级
4. **高性能**: 优化字节码生成和类加载过程，减少运行时开销
5. **安全性**: 提供安全的类恢复机制，防止测试污染生产环境

## 2. 关键技术要素

### 2.1 字节码操作技术

我们将同时使用Javassist和ASM进行字节码操作：

- **Javassist**: 作为主要字节码操作库，用于大部分场景
- **ASM**: 用于处理Javassist不能很好支持的JDK21特性

字节码操作将实现以下核心功能：
- 修改静态方法的字节码以支持Mock
- 改变私有方法的访问权限
- 拦截构造函数调用
- 替换final方法的实现

### 2.2 类加载与热替换

类加载与热替换是本框架的核心技术，主要包括：

1. **自定义类加载器**: 
   - 使用全限定名隔离不同版本的类
   - 支持按需重新加载类

2. **热替换机制**:
   - 在运行时替换类的实现
   - 使用JDK21的新特性优化热替换性能
   - 提供安全的恢复机制

3. **类隔离**:
   - 防止测试代码污染生产环境
   - 精确控制类的加载范围

### 2.3 JDK21优化特性

针对JDK21的特性优化包括：

1. **虚拟线程支持**:
   - 优化在虚拟线程环境下的性能
   - 提供特定的API以利用虚拟线程

2. **记录模式匹配**:
   - 使用记录模式匹配简化Mock定义
   - 提供更类型安全的API

3. **字符串模板**:
   - 使用字符串模板简化日志和错误消息

## 3. 架构设计

### 3.1 模块化设计

框架分为五个主要模块：

```
enhanced-mock-framework/
├── mock-basics/       # 基础概念和示例
├── mock-core/         # 核心功能实现
├── mock-advanced/     # 高级特性实现  
├── mock-utils/        # 工具和辅助功能
└── mock-integration/  # 与其他框架集成
```

### 3.2 核心组件

1. **BytecodeEnhancer**: 负责字节码修改的核心组件
   - `ClassTransformer`: 转换类的字节码
   - `MethodInterceptor`: 拦截方法调用
   - `ConstructorTransformer`: 处理构造函数

2. **MockClassLoader**: 自定义类加载器
   - `VersionedClassLoader`: 支持多JDK版本
   - `IsolationClassLoader`: 提供类隔离

3. **HotSwapper**: 热替换功能实现
   - `ClassSwapper`: 实现类的热替换
   - `ClassRestorer`: 恢复原始类

4. **MockAPI**: 用户界面API
   - `MockBuilder`: 构建Mock对象
   - `MockVerifier`: 验证Mock调用

## 4. 学习路径设计

我们设计了由浅入深的学习路径，帮助用户从基础到高级逐步掌握框架：

### 4.1 基础入门 (mock-basics)

1. **Mock基本概念**:
   - 什么是Mock以及为什么需要它
   - Mock与Stub的区别
   - 常见的Mock场景

2. **入门示例**:
   - 简单的手动Mock
   - 接口Mock实现
   - 基本的Mock验证

### 4.2 核心功能学习 (mock-core)

1. **字节码基础**:
   - Java字节码格式入门
   - Javassist基本用法
   - 类加载器工作原理

2. **基本Mock实现**:
   - 实现简单的方法拦截
   - 返回值设置与验证
   - 异常模拟

### 4.3 高级特性掌握 (mock-advanced)

1. **静态方法Mock**:
   - 字节码级别的静态方法替换
   - 静态方法验证

2. **私有方法Mock**:
   - 访问私有方法的技术
   - 私有方法的替换与验证

3. **构造函数和final方法**:
   - 拦截构造函数调用
   - 绕过final限制

### 4.4 工具与最佳实践 (mock-utils & mock-integration)

1. **热替换技术**:
   - 类热替换原理与实现
   - 安全地恢复类

2. **与其他框架集成**:
   - JUnit集成
   - Spring框架集成

3. **性能优化与最佳实践**:
   - 减少Mock对性能的影响
   - Mock使用的最佳实践

### 4.5 JDK21特性专题

1. **虚拟线程环境下的Mock**:
   - 在虚拟线程中使用Mock
   - 性能优化策略

2. **利用JDK21新特性**:
   - 使用记录模式匹配简化Mock
   - 字符串模板应用

## 5. 实现路线图

### 5.1 第一阶段: 基础设施 (1-2周)

- 搭建项目结构
- 实现基本的Mock概念示例
- 配置多版本JDK支持

### 5.2 第二阶段: 核心功能 (3-4周)

- 实现Javassist字节码操作
- 开发自定义类加载器
- 实现基本的方法拦截

### 5.3 第三阶段: 高级功能 (5-6周)

- 实现静态方法Mock
- 实现私有方法Mock
- 实现构造函数Mock

### 5.4 第四阶段: 工具与集成 (7-8周)

- 实现热替换功能
- 开发断言和验证工具
- 实现与JUnit的集成

### 5.5 第五阶段: JDK21优化 (9-10周)

- 优化JDK21环境下的性能
- 添加对虚拟线程的支持
- 利用JDK21新特性改进API

## 6. API设计示例

### 6.1 基本API示例

```java
// 创建简单Mock
ClassToMock mock = Mock.create(ClassToMock.class);

// 设置行为
Mock.when(mock.someMethod(anyString())).thenReturn("mocked result");

// 验证调用
Mock.verify(mock).someMethod("expected parameter");
```

### 6.2 静态方法Mock示例

```java
// 启用静态方法Mock
Mock.enableStaticMocking(ClassWithStaticMethods.class);

// 设置静态方法行为
Mock.whenStatic(ClassWithStaticMethods.staticMethod()).thenReturn("mocked static");

// 调用并验证
String result = ClassWithStaticMethods.staticMethod();
Mock.verifyStatic(ClassWithStaticMethods.class).staticMethod();
```

### 6.3 JDK21特性示例

```java
// 使用记录模式匹配的Mock定义
Mock.when(person instanceof Person(var name, var age) && age > 30)
    .thenAnswer(invocation -> {
        Person p = invocation.getArgument(0);
        return p.withAge(p.age() - 10);
    });

// 使用虚拟线程的Mock测试
try (var scope = new MockScope()) {
    Thread.startVirtualThread(() -> {
        // 在虚拟线程中的Mock行为
        ClassToMock threadMock = scope.create(ClassToMock.class);
        scope.when(threadMock.method()).thenReturn("thread result");
    }).join();
}
```

## 7. 与其他框架的对比

| 特性 | 本框架 | PowerMock | Mockito |
|------|-------|-----------|---------|
| JDK21支持 | ✅ (优先) | ❌ | ⚠️ (部分) |
| 静态方法Mock | ✅ | ✅ | ❌ (基本版) |
| 私有方法Mock | ✅ | ✅ | ❌ |
| 构造函数Mock | ✅ | ✅ | ❌ |
| 类热替换 | ✅ | ⚠️ (部分) | ❌ |
| 易用性 | ⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐⭐ |
| 性能 | ⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐⭐ |
| 学习曲线 | 渐进式 | 陡峭 | 平缓 |

## 8. 项目价值与创新点

1. **JDK21优先支持**: 针对最新JDK优化，利用新特性提升性能
2. **渐进式学习**: 从基础到高级的学习路径，降低学习门槛
3. **安全可靠**: 提供安全的类恢复机制，防止测试污染
4. **性能优化**: 使用多种技术减少运行时开销
5. **现代API**: 使用最新Java特性设计简洁、类型安全的API

## 9. 风险管理

1. **JDK兼容性风险**:
   - 风险: 不同JDK版本的字节码格式和类加载机制有差异
   - 对策: 为JDK21设计优化实现，同时提供向下兼容方案

2. **Javassist局限性**:
   - 风险: Javassist可能对JDK21中的某些特性支持不足
   - 对策: 必要时结合ASM等其他字节码库

3. **性能风险**:
   - 风险: 过多的字节码操作可能导致性能下降
   - 对策: 利用JDK21的新特性优化性能，实现缓存机制

4. **安全风险**:
   - 风险: 字节码修改和类加载器可能引入安全问题
   - 对策: 严格限制修改范围，提供安全的恢复机制

## 10. 总结

本框架通过优先支持JDK21、提供渐进式学习路径和安全可靠的实现，为Java开发者提供了一个现代化的增强型Mock框架。不仅可以支持静态方法、私有方法和构造函数的Mock，还能够利用最新的JDK特性提升性能和用户体验。同时，框架的设计使新手也能够循序渐进地掌握Mock技术，提高测试效率和代码质量。 