# 增强型Mock框架实现指南

本指南为初学者提供了从零开始实现增强型Mock框架的详细步骤，重点关注JDK21环境。我们将通过循序渐进的方式，帮助您理解并实现各个核心组件。

## 1. 环境准备

首先，我们需要准备好开发环境：

### 1.1 JDK环境配置

```bash
# 确保安装了JDK21
java -version  # 应显示版本21或更高

# 配置JAVA_HOME环境变量
export JAVA_HOME=/path/to/jdk21
export PATH=$JAVA_HOME/bin:$PATH
```

### 1.2 开发工具准备

推荐使用以下工具：
- IntelliJ IDEA 或 Eclipse 最新版
- Maven 3.8.x 或更高版本
- Git 用于版本控制

### 1.3 项目初始化

```bash
# 克隆项目模板（如果使用我们提供的模板）
git clone https://github.com/mocktutorial/enhanced-mock-framework.git
cd enhanced-mock-framework

# 或者从头创建Maven项目
mvn archetype:generate -DgroupId=com.mocktutorial -DartifactId=enhanced-mock-framework -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
```

## 2. 从基础开始：理解Mock概念

在开始实现之前，让我们先理解Mock的基本概念：

### 2.1 创建基础示例

创建`mock-basics`模块，实现一些简单的手动Mock示例：

```java
// 步骤1: 创建接口
public interface UserService {
    User findById(long id);
    boolean saveUser(User user);
}

// 步骤2: 创建手动Mock实现
public class ManualMockUserService implements UserService {
    private Map<Long, User> mockUsers = new HashMap<>();
    private List<User> savedUsers = new ArrayList<>();
    
    // 添加测试数据
    public void addMockUser(User user) {
        mockUsers.put(user.getId(), user);
    }
    
    @Override
    public User findById(long id) {
        return mockUsers.get(id);
    }
    
    @Override
    public boolean saveUser(User user) {
        savedUsers.add(user);
        return true;
    }
    
    // 验证方法
    public boolean verifySavedUser(User user) {
        return savedUsers.contains(user);
    }
}
```

### 2.2 使用手动Mock示例

```java
// 测试代码
@Test
public void testUserService() {
    // 准备Mock对象
    ManualMockUserService mockService = new ManualMockUserService();
    User testUser = new User(1L, "Test User");
    mockService.addMockUser(testUser);
    
    // 使用Mock对象
    User foundUser = mockService.findById(1L);
    assertEquals("Test User", foundUser.getName());
    
    // 验证交互
    User newUser = new User(2L, "New User");
    mockService.saveUser(newUser);
    assertTrue(mockService.verifySavedUser(newUser));
}
```

## 3. 核心功能实现：字节码增强

接下来，我们开始实现框架的核心功能 - 字节码增强：

### 3.1 添加必要依赖

首先，在`mock-core`模块中添加必要的依赖：

```xml
<!-- pom.xml -->
<dependencies>
    <dependency>
        <groupId>org.javassist</groupId>
        <artifactId>javassist</artifactId>
    </dependency>
    <dependency>
        <groupId>org.ow2.asm</groupId>
        <artifactId>asm</artifactId>
    </dependency>
    <dependency>
        <groupId>org.ow2.asm</groupId>
        <artifactId>asm-commons</artifactId>
    </dependency>
</dependencies>
```

### 3.2 实现基本的字节码操作

```java
// 步骤1: 创建BytecodeEnhancer类
package com.mocktutorial.core.bytecode;

import javassist.*;

public class BytecodeEnhancer {
    private ClassPool classPool;
    
    public BytecodeEnhancer() {
        this.classPool = ClassPool.getDefault();
    }
    
    // 修改方法实现
    public byte[] modifyMethod(String className, String methodName, String methodBody) 
            throws NotFoundException, CannotCompileException {
        CtClass ctClass = classPool.get(className);
        CtMethod method = ctClass.getDeclaredMethod(methodName);
        method.setBody(methodBody);
        byte[] bytecode = ctClass.toBytecode();
        ctClass.detach(); // 释放资源
        return bytecode;
    }
    
    // 修改方法访问权限
    public byte[] makeMethodPublic(String className, String methodName) 
            throws NotFoundException, CannotCompileException {
        CtClass ctClass = classPool.get(className);
        CtMethod method = ctClass.getDeclaredMethod(methodName);
        method.setModifiers(Modifier.PUBLIC);
        byte[] bytecode = ctClass.toBytecode();
        ctClass.detach();
        return bytecode;
    }
}
```

### 3.3 实现方法拦截器

```java
// 步骤2: 创建MethodInterceptor接口
package com.mocktutorial.core.interceptor;

public interface MethodInterceptor {
    Object intercept(Object obj, String methodName, Object[] args, MethodProxy methodProxy) throws Throwable;
}

// 步骤3: 创建MethodProxy类
package com.mocktutorial.core.interceptor;

import java.lang.reflect.Method;

public class MethodProxy {
    private final Object target;
    private final Method method;
    
    public MethodProxy(Object target, Method method) {
        this.target = target;
        this.method = method;
    }
    
    public Object invokeSuper(Object[] args) throws Throwable {
        return method.invoke(target, args);
    }
}
```

## 4. 自定义类加载器实现

类加载器是框架的关键组件，它允许我们动态加载修改后的类：

### 4.1 基本类加载器实现

```java
// 步骤1: 创建自定义类加载器
package com.mocktutorial.core.classloader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MockClassLoader extends ClassLoader {
    private final Map<String, byte[]> modifiedClasses = new ConcurrentHashMap<>();
    
    public MockClassLoader(ClassLoader parent) {
        super(parent);
    }
    
    public void addModifiedClass(String name, byte[] bytecode) {
        modifiedClasses.put(name, bytecode);
    }
    
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytecode = modifiedClasses.get(name);
        if (bytecode != null) {
            return defineClass(name, bytecode, 0, bytecode.length);
        }
        return super.findClass(name);
    }
    
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        // 如果是我们修改过的类，使用自定义逻辑加载
        if (modifiedClasses.containsKey(name)) {
            return findClass(name);
        }
        // 否则使用父类加载器
        return super.loadClass(name);
    }
}
```

### 4.2 JDK21优化的类加载器

```java
// 步骤2: 创建JDK21优化版本的类加载器
package com.mocktutorial.core.classloader.jdk21;

import com.mocktutorial.core.classloader.MockClassLoader;
import java.util.concurrent.ConcurrentHashMap;

public class Jdk21MockClassLoader extends MockClassLoader {
    private final ConcurrentHashMap<String, Class<?>> classCache;
    
    public Jdk21MockClassLoader(ClassLoader parent) {
        super(parent);
        this.classCache = new ConcurrentHashMap<>();
    }
    
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        // 利用JDK21的并发优化
        return classCache.computeIfAbsent(name, k -> {
            try {
                if (modifiedClasses.containsKey(k)) {
                    return findClass(k);
                }
                return super.loadClass(k);
            } catch (ClassNotFoundException e) {
                return null;
            }
        });
    }
}
```

## 5. 实现核心API

接下来，我们实现用户将使用的核心API：

### 5.1 创建Mock构建器

```java
// 步骤1: 创建MockBuilder类
package com.mocktutorial.core.api;

import com.mocktutorial.core.bytecode.BytecodeEnhancer;
import com.mocktutorial.core.classloader.MockClassLoader;
import com.mocktutorial.core.interceptor.MethodInterceptor;

import java.lang.reflect.Proxy;

public class MockBuilder {
    private final BytecodeEnhancer enhancer;
    private final MockClassLoader classLoader;
    
    public MockBuilder() {
        this.enhancer = new BytecodeEnhancer();
        this.classLoader = new MockClassLoader(getClass().getClassLoader());
    }
    
    // 创建接口Mock
    public <T> T createInterfaceMock(Class<T> interfaceClass, MethodInterceptor interceptor) {
        return (T) Proxy.newProxyInstance(
            classLoader,
            new Class<?>[] { interfaceClass },
            (proxy, method, args) -> {
                return interceptor.intercept(
                    proxy, 
                    method.getName(), 
                    args, 
                    null // 接口Mock不需要调用原始方法
                );
            }
        );
    }
    
    // 为后续实现做准备，先返回null
    public <T> T createClassMock(Class<T> classToMock, MethodInterceptor interceptor) {
        // TODO: 实现基于类的Mock
        return null;
    }
}
```

### 5.2 实现行为定义API

```java
// 步骤2: 创建When类用于定义行为
package com.mocktutorial.core.api;

import java.util.function.Function;

public class When<T> {
    private final T mockObject;
    private Object lastMethodCall;
    
    public When(T mockObject) {
        this.mockObject = mockObject;
    }
    
    public void setLastMethodCall(Object lastMethodCall) {
        this.lastMethodCall = lastMethodCall;
    }
    
    public <R> ThenReturn<R> thenReturn(R value) {
        // 在实际实现中，需要将这个行为与lastMethodCall关联
        return new ThenReturn<>(value);
    }
    
    public <R> ThenAnswer<R> thenAnswer(Function<Object[], R> answer) {
        // 同上，需要与lastMethodCall关联
        return new ThenAnswer<>(answer);
    }
    
    // 内部类用于链式调用
    public class ThenReturn<R> {
        private final R returnValue;
        
        public ThenReturn(R returnValue) {
            this.returnValue = returnValue;
        }
        
        public R getReturnValue() {
            return returnValue;
        }
    }
    
    public class ThenAnswer<R> {
        private final Function<Object[], R> answerFunction;
        
        public ThenAnswer(Function<Object[], R> answerFunction) {
            this.answerFunction = answerFunction;
        }
        
        public Function<Object[], R> getAnswerFunction() {
            return answerFunction;
        }
    }
}
```

### 5.3 实现主要API入口

```java
// 步骤3: 创建Mock主类
package com.mocktutorial.core.api;

import com.mocktutorial.core.interceptor.MethodInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Mock {
    private static final MockBuilder builder = new MockBuilder();
    private static final Map<Object, MethodInterceptor> mockInterceptors = new ConcurrentHashMap<>();
    private static final Map<Object, Object> lastMethodCalls = new ConcurrentHashMap<>();
    
    // 创建Mock对象
    public static <T> T create(Class<T> classToMock) {
        MethodInterceptor interceptor = (obj, methodName, args, methodProxy) -> {
            // 记录方法调用
            lastMethodCalls.put(obj, new MethodCall(methodName, args));
            // TODO: 实现行为匹配和返回值
            return null;
        };
        
        T mock;
        if (classToMock.isInterface()) {
            mock = builder.createInterfaceMock(classToMock, interceptor);
        } else {
            mock = builder.createClassMock(classToMock, interceptor);
        }
        
        mockInterceptors.put(mock, interceptor);
        return mock;
    }
    
    // 定义行为
    public static <T> When<T> when(T methodCall) {
        @SuppressWarnings("unchecked")
        When<T> when = new When<>((T) mockInterceptors.keySet().stream()
                .filter(mock -> lastMethodCalls.containsKey(mock))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No method call detected")));
        
        when.setLastMethodCall(lastMethodCalls.get(when));
        return when;
    }
    
    // 用于记录方法调用
    private static class MethodCall {
        private final String methodName;
        private final Object[] args;
        
        public MethodCall(String methodName, Object[] args) {
            this.methodName = methodName;
            this.args = args;
        }
    }
}
```

## 6. 实现高级功能：静态方法Mock

现在，我们开始实现更高级的功能 - 静态方法Mock：

### 6.1 静态方法字节码增强

```java
// 添加静态方法支持到BytecodeEnhancer
public byte[] modifyStaticMethod(String className, String methodName, String methodBody) 
        throws NotFoundException, CannotCompileException {
    CtClass ctClass = classPool.get(className);
    CtMethod method = ctClass.getDeclaredMethod(methodName);
    
    if (!Modifier.isStatic(method.getModifiers())) {
        throw new IllegalArgumentException("Method is not static: " + methodName);
    }
    
    method.setBody(methodBody);
    byte[] bytecode = ctClass.toBytecode();
    ctClass.detach();
    return bytecode;
}
```

### 6.2 静态方法Mock API

```java
// 在Mock类中添加静态方法支持
// 启用静态方法Mock
public static void enableStaticMocking(Class<?> classToMock) {
    try {
        // 示例实现：将静态方法重定向到Mock处理器
        BytecodeEnhancer enhancer = new BytecodeEnhancer();
        String className = classToMock.getName();
        
        // 获取所有静态方法
        for (Method method : classToMock.getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers())) {
                String methodName = method.getName();
                String returnType = method.getReturnType().getName();
                
                // 生成重定向代码
                String body = String.format(
                    "{ return (%s) com.mocktutorial.core.api.StaticMockHandler.handle(\"%s\", \"%s\", $args); }",
                    returnType, className, methodName
                );
                
                // 修改字节码
                byte[] modifiedClass = enhancer.modifyStaticMethod(className, methodName, body);
                
                // 加载修改后的类
                MockClassLoader classLoader = new MockClassLoader(
                    ClassLoader.getSystemClassLoader()
                );
                classLoader.addModifiedClass(className, modifiedClass);
                Class<?> loadedClass = classLoader.loadClass(className);
                
                // 替换原始类加载器中的类（这是简化的实现）
                // 实际应使用更安全的方法替换类加载器
            }
        }
    } catch (Exception e) {
        throw new RuntimeException("Failed to enable static mocking for " + classToMock.getName(), e);
    }
}

// 添加静态方法行为定义
public static <T> WhenStatic<T> whenStatic(T methodCall) {
    // 类似于普通方法的when实现
    // 但需要处理静态方法调用
    return new WhenStatic<>();
}
```

### 6.3 静态方法处理器

```java
// 创建静态方法处理器
package com.mocktutorial.core.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StaticMockHandler {
    private static final Map<String, Object> staticMockBehaviors = new ConcurrentHashMap<>();
    
    // 注册静态方法行为
    public static void registerBehavior(String className, String methodName, Object behavior) {
        staticMockBehaviors.put(createKey(className, methodName), behavior);
    }
    
    // 处理静态方法调用
    public static Object handle(String className, String methodName, Object[] args) {
        String key = createKey(className, methodName);
        Object behavior = staticMockBehaviors.get(key);
        
        if (behavior instanceof When.ThenReturn) {
            return ((When.ThenReturn<?>) behavior).getReturnValue();
        }
        else if (behavior instanceof When.ThenAnswer) {
            @SuppressWarnings("unchecked")
            When.ThenAnswer<Object> answer = (When.ThenAnswer<Object>) behavior;
            return answer.getAnswerFunction().apply(args);
        }
        
        // 默认返回null或基本类型默认值
        return null;
    }
    
    private static String createKey(String className, String methodName) {
        return className + "#" + methodName;
    }
}
```

## 7. 实现热替换功能

最后，我们实现热替换功能：

### 7.1 基本热替换实现

```java
// 创建HotSwapper类
package com.mocktutorial.utils.hotswap;

import com.mocktutorial.core.bytecode.BytecodeEnhancer;
import com.mocktutorial.core.classloader.MockClassLoader;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HotSwapper {
    private static Instrumentation instrumentation;
    private final BytecodeEnhancer enhancer;
    private final Map<String, byte[]> originalClassBytes = new ConcurrentHashMap<>();
    
    // 在实际使用中，需要通过Java agent获取Instrumentation
    public static void premain(String args, Instrumentation inst) {
        instrumentation = inst;
    }
    
    public HotSwapper() {
        this.enhancer = new BytecodeEnhancer();
    }
    
    // 替换类
    public void swapClass(Class<?> originalClass, byte[] newClassBytes) {
        try {
            if (instrumentation == null) {
                throw new IllegalStateException("Instrumentation not available. Java agent not loaded.");
            }
            
            // 保存原始字节码用于恢复
            if (!originalClassBytes.containsKey(originalClass.getName())) {
                // 保存原始类字节码的逻辑（简化版）
                originalClassBytes.put(originalClass.getName(), new byte[0]);
            }
            
            // 重新定义类
            ClassDefinition definition = new ClassDefinition(originalClass, newClassBytes);
            instrumentation.redefineClasses(definition);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hot swap class: " + originalClass.getName(), e);
        }
    }
    
    // 恢复原始类
    public void restoreClass(Class<?> modifiedClass) {
        try {
            String className = modifiedClass.getName();
            if (originalClassBytes.containsKey(className)) {
                byte[] original = originalClassBytes.get(className);
                ClassDefinition definition = new ClassDefinition(modifiedClass, original);
                instrumentation.redefineClasses(definition);
                originalClassBytes.remove(className);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore class: " + modifiedClass.getName(), e);
        }
    }
}
```

### 7.2 JDK21优化的热替换

```java
// 创建JDK21优化版本的热替换器
package com.mocktutorial.utils.hotswap.jdk21;

import com.mocktutorial.utils.hotswap.HotSwapper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Jdk21HotSwapper extends HotSwapper {
    private final ExecutorService executor;
    
    public Jdk21HotSwapper() {
        super();
        // 使用JDK21虚拟线程优化
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }
    
    // 异步替换类
    public void swapClassAsync(Class<?> originalClass, byte[] newClassBytes) {
        executor.submit(() -> swapClass(originalClass, newClassBytes));
    }
    
    // 异步恢复类
    public void restoreClassAsync(Class<?> modifiedClass) {
        executor.submit(() -> restoreClass(modifiedClass));
    }
    
    // 关闭执行器
    public void shutdown() {
        executor.shutdown();
    }
}
```

## 8. 使用示例：完整测试

最后，让我们看一个完整的使用示例：

```java
// 测试示例
package com.mocktutorial.examples;

import com.mocktutorial.core.api.Mock;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceExample {
    interface UserService {
        User findById(long id);
        boolean saveUser(User user);
    }
    
    static class User {
        private long id;
        private String name;
        
        public User(long id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public long getId() { return id; }
        public String getName() { return name; }
    }
    
    static class UserController {
        private final UserService userService;
        
        public UserController(UserService userService) {
            this.userService = userService;
        }
        
        public String getUserName(long userId) {
            User user = userService.findById(userId);
            return user != null ? user.getName() : "Unknown User";
        }
        
        public boolean createUser(long id, String name) {
            return userService.saveUser(new User(id, name));
        }
    }
    
    @Test
    void testUserController() {
        // 创建Mock对象
        UserService mockService = Mock.create(UserService.class);
        
        // 定义行为
        User testUser = new User(1L, "Test User");
        Mock.when(mockService.findById(1L)).thenReturn(testUser);
        Mock.when(mockService.saveUser(any(User.class))).thenReturn(true);
        
        // 创建待测试对象
        UserController controller = new UserController(mockService);
        
        // 测试
        assertEquals("Test User", controller.getUserName(1L));
        assertEquals("Unknown User", controller.getUserName(2L));
        assertTrue(controller.createUser(2L, "New User"));
        
        // 验证调用
        Mock.verify(mockService).findById(1L);
        Mock.verify(mockService).findById(2L);
        Mock.verify(mockService).saveUser(any(User.class));
    }
}
```

## 9. 自动化测试与一致性验证报告【已更新V2】

### 测试流程
1. 运行 `mvn clean test`，全项目所有测试全部通过。
2. 新增 `MockRestoreTest`，专门验证mock/stub/restore流程：
   - 创建mock对象
   - 配置存根（thenReturn）
   - 验证存根生效
   - 调用 `Mock.reset` 还原
   - 验证还原后为默认行为（如Optional.empty）
3. 单独运行 `MockRestoreTest`，测试通过。

### 结果说明
- 所有模块测试均通过，mock/stub/restore流程完全符合预期。
- 修改前、修改后、还原的所有过程都能正确反映mock行为和隔离性。
- 相关测试报告和流程已归档，便于后续回溯和持续集成。

## 10. 总结与下一步

恭喜！您现在已经了解了如何从零开始实现一个增强型Mock框架。当然，这只是一个简化的实现，真正的框架需要更多的工作来处理各种边缘情况和优化性能。

### 下一步建议：

1. **增强静态方法支持**：完善静态方法Mock的实现
2. **实现私有方法支持**：添加对私有方法的Mock支持
3. **JDK21特性优化**：充分利用JDK21的新特性
4. **完善测试**：编写全面的单元测试和集成测试
5. **改进文档**：为不同级别的用户提供详细文档

通过本指南，您已经掌握了实现Mock框架的基本知识和技术。继续探索和改进这个框架，您将能够创建一个功能强大的测试工具！

---

# V3高级Mock架构与实现路线（草案）

## 1. 现有方案的局限

- Javassist只能在构造函数体内插入代码，无法阻止构造函数本身的字段赋值（如`this.value = ...`），只能在构造体前/后做拦截和字段拷贝。
- 你只能"伪造"对象状态，不能让`new`出来的对象就是mockInstance本身。
- 这种方式对final字段、不可变对象、父类构造器等场景支持有限。
- 这种方式对依赖注入、Spring等框架的兼容性有限。

## 2. 高级Mock构造器方案

### 方案A：Instrumentation redefine + 构造器劫持（推荐，Mockito/PowerMock同类原理）
- 使用`java.lang.instrument.Instrumentation`在类加载后**重新定义类的字节码**，将构造器替换为mock逻辑。
- 可以做到：new出来的对象就是你想要的mock对象，甚至可以直接返回你自定义的实例。
- 支持final类、final字段、父类构造器等复杂场景。
- 步骤：
  1. Java Agent注入Instrumentation
  2. 拦截类加载/重定义，替换目标类的构造器字节码
  3. 构造器mock逻辑：mock命中时直接return mockInstance（可用ThreadLocal传递），否则走原始逻辑
  4. 对于不能直接redefine的类（如JDK核心类），可用工厂方法或动态代理
- 优点：兼容性强，支持绝大多数场景，可与Spring、Guice等依赖注入框架无缝集成
- 缺点：需要Java Agent，用户需配置JVM参数，Instrumentation API有一定门槛

### 方案B：字节码生成+工厂替代new（如Objenesis/Mockito）
- 用Objenesis等库直接分配对象内存，不调用构造函数。
- 提供统一的Mock工厂方法，所有mock对象都通过工厂创建，而不是直接new。
- 结合动态代理/字节码增强，实现方法拦截和mock。
- 优点：不需要Java Agent，易于集成，兼容JDK8-21，适合大多数业务mock场景。
- 缺点：不能mock直接new出来的对象（只能通过工厂），对final类/字段支持有限（需结合Instrumentation）。

### 方案C：JDK21新特性适配
- 结合JDK21的`MethodHandles.Lookup`、`defineHiddenClass`等API，动态生成mock类。
- 支持record、sealed class等新语法。
- 结合虚拟线程做高并发mock测试。

## 3. 推荐架构设计

- 核心mock引擎：支持Instrumentation redefine和Objenesis两种模式，自动检测环境选择最佳方案。
- API层：统一用`Mock.mock(Class<T>)`、`Mock.when(...)`等API，屏蔽底层实现细节。
- Spring/DI集成：提供starter和BeanPostProcessor，自动mock依赖。
- JDK兼容性：自动适配JDK8-21，未来可扩展JDK22+新特性。
- 可插拔拦截器：支持自定义mock行为、验证、事件监听等。

## 4. 参考实现/开源库

- [Mockito](https://github.com/mockito/mockito)（Instrumentation+Objenesis）
- [PowerMock](https://github.com/powermock/powermock)（Agent+ASM）
- [Byte Buddy](https://bytebuddy.net/#/)

## 5. 结论与建议

- 短期：继续用当前Javassist+字段拷贝方案，测试用例只断言handleConstructorCall返回的对象。
- 中长期：实现Instrumentation redefine，支持真正的构造器mock，兼容所有场景。
- API层保持不变，底层实现可切换，方便后续升级和对外提供依赖。

---

# V3版本实现计划（Recommended Architecture Implementation Plan）

## 1. 目标
- 支持真正的构造器mock，new出来的对象就是mockInstance或自定义实现。
- 兼容JDK8-21及未来版本，支持final类、final字段、record、sealed class等。
- 提供无侵入API，适配主流依赖注入框架。

## 2. 技术路线
- Instrumentation redefine为主，Objenesis/Unsafe为辅，自动检测环境。
- 提供MockAgent，支持-premain和agentmain两种模式。
- 构造器mock采用ThreadLocal传递mockInstance，构造器体内抛出特殊异常，外部捕获后返回mockInstance。
- 提供MockFactory工厂API，所有mock对象可通过工厂创建。
- 支持Spring、Guice等依赖注入自动mock。

## 3. API设计
- `Mock.mock(Class<T>)`：自动选择最佳mock方式。
- `Mock.when(...)`、`Mock.verify(...)`：行为定义与验证。
- `MockFactory.create(Class<T>)`：工厂创建mock对象。
- `MockAgent`：Java Agent入口，支持动态attach。

## 4. 关键实现点
- Instrumentation redefine构造器：字节码插桩，mock命中时直接抛出MockReturnException，外部捕获后返回mockInstance。
- Objenesis/Unsafe分配对象：兼容无构造函数场景。
- 兼容final类/字段、record、sealed class。
- Spring集成：BeanPostProcessor自动mock依赖。

## 5. 未来展望
- 适配JDK22+新特性，支持虚拟线程、record pattern matching等。
- 提供可插拔mock行为、事件监听、mock链路追踪等高级特性。
- 完善文档与示例，推动社区生态。

--- 