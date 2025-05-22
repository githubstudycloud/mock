package com.mocktutorial.core.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mocktutorial.core.Mock;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * 【已更新V2】
 * 当未启用增强mock时，作为回退方案的适配器。接口类型用JDK Proxy实现，类类型仅支持无参构造简单mock。
 * <p>
 * 设计要点：
 * <ul>
 *   <li>所有行为存根和调用记录均为每个mock实例独立，无全局状态。</li>
 *   <li>支持链式API，行为存根优先于默认返回值。</li>
 *   <li>集合类型方法（isEmpty/size/contains/iterator）有特殊默认返回，避免NPE。</li>
 * </ul>
 * <p>
 * 典型用法：
 * <pre>
 *   List mockList = Mock.mock(List.class);
 *   Mock.when(mockList, "size").thenReturn(10);
 *   assertEquals(10, mockList.size());
 * </pre>
 */
public class MockitoAdapter {
    private static final Logger logger = LoggerFactory.getLogger(MockitoAdapter.class);
    
    /**
     * 【已更新V2】
     * 创建指定类型的mock对象。接口用JDK Proxy，类仅支持无参构造。
     * @param <T> 要mock的类型
     * @param classToMock 要mock的类或接口
     * @return mock实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T createMock(Class<T> classToMock) {
        if (classToMock.isInterface()) {
            // 接口类型用JDK Proxy
            return (T) Proxy.newProxyInstance(
                    classToMock.getClassLoader(),
                    new Class<?>[] { classToMock },
                    new MockInvocationHandler(classToMock)
            );
        } else {
            // 类类型仅支持无参构造
            try {
                Constructor<T> constructor = classToMock.getDeclaredConstructor();
                constructor.setAccessible(true);
                return constructor.newInstance();
            } catch (Exception e) {
                logger.error("Failed to create simple mock for class: " + classToMock.getName(), e);
                return null;
            }
        }
    }
    
    /**
     * 【已更新V2】
     * JDK Proxy的InvocationHandler实现，支持行为存根、调用记录、默认返回值。
     * <ul>
     *   <li>存根优先：如有thenReturn/thenThrow/thenImplement，优先返回存根结果。</li>
     *   <li>集合方法特殊处理：isEmpty/size/contains/iterator有合理默认值。</li>
     *   <li>所有行为和调用记录均为本mock实例独立。</li>
     * </ul>
     */
    public static class MockInvocationHandler implements InvocationHandler {
        private final Class<?> mockedInterface;
        private final Map<String, Object> methodReturns = new HashMap<>();
        public final Map<MethodCallKey, java.util.List<Object[]>> methodCalls = new HashMap<>();
        // 新增：记录每个方法调用的存根行为（用于存根）
        public final Map<MethodCallKey, StubBehavior> stubs = new HashMap<>();
        /**
         * 【已更新V2】
         * 方法调用唯一标识，包含方法名和参数。
         */
        public static class MethodCallKey {
            private final String methodName;
            private final Object[] args;
            public MethodCallKey(String methodName, Object[] args) {
                this.methodName = methodName;
                this.args = args == null ? new Object[0] : args.clone();
            }
            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                MethodCallKey that = (MethodCallKey) o;
                return methodName.equals(that.methodName) && java.util.Arrays.deepEquals(args, that.args);
            }
            @Override
            public int hashCode() {
                int result = methodName.hashCode();
                result = 31 * result + java.util.Arrays.deepHashCode(args);
                return result;
            }
        }
        /**
         * 【已更新V2】
         * 存根行为封装，支持返回值、异常、实现函数。
         */
        public static class StubBehavior {
            Object returnValue;
            Throwable throwable;
            java.util.function.Function<Object[], Object> implementation;
        }
        
        MockInvocationHandler(Class<?> mockedInterface) {
            this.mockedInterface = mockedInterface;
        }
        
        /**
         * 【已更新V2】
         * 方法调用拦截逻辑：优先返回存根，其次处理集合方法，最后返回类型默认值。
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // equals/hashCode/toString特殊处理
            if (method.getName().equals("equals")) {
                return proxy == args[0];
            } else if (method.getName().equals("hashCode")) {
                return System.identityHashCode(proxy);
            } else if (method.getName().equals("toString")) {
                return "Mock of " + mockedInterface.getName() + "@" + Integer.toHexString(System.identityHashCode(proxy));
            }
            // 记录方法调用（用于验证）
            MethodCallKey callKey = new MethodCallKey(method.getName(), args);
            methodCalls.computeIfAbsent(callKey, k -> new java.util.ArrayList<>()).add(args);
            // 1. 优先查找 stub
            StubBehavior stub = stubs.get(callKey);
            if (stub != null) {
                if (stub.throwable != null) throw stub.throwable;
                if (stub.implementation != null) return stub.implementation.apply(args);
                return stub.returnValue;
            }
            // 2. 处理 Collection 的常用方法
            String methodName = method.getName();
            if (java.util.Collection.class.isAssignableFrom(mockedInterface) ||
                java.util.List.class.isAssignableFrom(mockedInterface) ||
                java.util.Set.class.isAssignableFrom(mockedInterface)) {
                if ("isEmpty".equals(methodName)) {
                    return true;
                } else if ("size".equals(methodName)) {
                    return 0;
                } else if ("contains".equals(methodName)) {
                    return false;
                } else if ("iterator".equals(methodName)) {
                    return java.util.Collections.emptyIterator();
                }
            }
            // 3. 默认返回值
            Object result = getDefaultReturnValue(method.getReturnType());
            return result;
        }
        
        /**
         * 【已更新V2】
         * 获取方法返回类型的默认值。对Optional/集合/布尔等类型有合理默认。
         */
        public Object getDefaultReturnValue(Class<?> returnType) {
            if (returnType.equals(void.class)) {
                return null;
            } else if (returnType.equals(boolean.class) || returnType.equals(Boolean.class)) {
                return false;
            } else if (returnType.equals(char.class) || returnType.equals(Character.class)) {
                return (char) 0;
            } else if (returnType.isPrimitive() || Number.class.isAssignableFrom(returnType)) {
                if (returnType.equals(byte.class) || returnType.equals(Byte.class)) {
                    return (byte) 0;
                } else if (returnType.equals(short.class) || returnType.equals(Short.class)) {
                    return (short) 0;
                } else if (returnType.equals(int.class) || returnType.equals(Integer.class)) {
                    return 0;
                } else if (returnType.equals(long.class) || returnType.equals(Long.class)) {
                    return 0L;
                } else if (returnType.equals(float.class) || returnType.equals(Float.class)) {
                    return 0.0f;
                } else if (returnType.equals(double.class) || returnType.equals(Double.class)) {
                    return 0.0d;
                }
            } else if (returnType.getName().equals("java.util.Optional") ||
                       (returnType.getPackage() != null && returnType.getPackage().getName().equals("java.util") && returnType.getSimpleName().equals("Optional"))) {
                // 兼容 Optional
                try {
                    return returnType.getMethod("empty").invoke(null);
                } catch (Exception e) {
                    return null;
                }
            } else if (java.util.List.class.isAssignableFrom(returnType)) {
                return java.util.Collections.emptyList();
            } else if (java.util.Set.class.isAssignableFrom(returnType)) {
                return java.util.Collections.emptySet();
            } else if (java.util.Map.class.isAssignableFrom(returnType)) {
                return java.util.Collections.emptyMap();
            }
            return null;
        }

        // 存根注册方法（供Mock/MethodInterceptor调用）
        public void setStub(String methodName, Object[] args, Object returnValue) {
            MethodCallKey key = new MethodCallKey(methodName, args);
            StubBehavior stub = new StubBehavior();
            stub.returnValue = returnValue;
            stubs.put(key, stub);
        }
        public void setStubThrow(String methodName, Object[] args, Throwable throwable) {
            MethodCallKey key = new MethodCallKey(methodName, args);
            StubBehavior stub = new StubBehavior();
            stub.throwable = throwable;
            stubs.put(key, stub);
        }
        public void setStubImpl(String methodName, Object[] args, java.util.function.Function<Object[], Object> impl) {
            MethodCallKey key = new MethodCallKey(methodName, args);
            StubBehavior stub = new StubBehavior();
            stub.implementation = impl;
            stubs.put(key, stub);
        }
        // 静态注册入口（便于 Mock/MethodInterceptor 调用）
        public static void stubReturn(Object mock, String methodName, Object[] args, Object returnValue) {
            InvocationHandler handler = Proxy.getInvocationHandler(mock);
            if (handler instanceof MockInvocationHandler) {
                ((MockInvocationHandler) handler).setStub(methodName, args, returnValue);
            }
        }
        public static void stubThrow(Object mock, String methodName, Object[] args, Throwable throwable) {
            InvocationHandler handler = Proxy.getInvocationHandler(mock);
            if (handler instanceof MockInvocationHandler) {
                ((MockInvocationHandler) handler).setStubThrow(methodName, args, throwable);
            }
        }
        public static void stubImpl(Object mock, String methodName, Object[] args, java.util.function.Function<Object[], Object> impl) {
            InvocationHandler handler = Proxy.getInvocationHandler(mock);
            if (handler instanceof MockInvocationHandler) {
                ((MockInvocationHandler) handler).setStubImpl(methodName, args, impl);
            }
        }
    }
} 