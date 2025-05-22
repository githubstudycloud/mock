package com.mocktutorial.core;

import com.mocktutorial.core.internal.MockitoAdapter;
import com.mocktutorial.core.internal.MethodInterceptor;
import com.mocktutorial.core.internal.MockCreator;
import com.mocktutorial.core.internal.MockSettings;

/**
 * 【已更新V2】
 * Enhanced Mock 框架的主入口。所有mock对象的创建、行为存根、验证等操作均从此类开始。
 * <p>
 * 设计理念：
 * <ul>
 *   <li>所有mock行为和调用记录均为"每个mock实例独立"，无全局/ThreadLocal状态，测试隔离性强。</li>
 *   <li>API风格统一，链式调用，易于新手理解和使用。</li>
 *   <li>支持接口和类的mock，静态/私有/构造函数mock需参考高级用法。</li>
 * </ul>
 * <p>
 * 典型用法：
 * <pre>
 *   UserService mock = Mock.mock(UserService.class);
 *   Mock.when(mock, "findById", 1L).thenReturn(Optional.of(user));
 *   Mock.verify(mock).once().findById(1L);
 * </pre>
 */
public class Mock {
    
    /**
     * 【已更新V2】
     * 创建指定类型的mock对象。mock对象的行为和调用记录仅影响本实例。
     * @param <T> 要mock的类型
     * @param classToMock 要mock的类或接口
     * @return mock实例
     */
    public static <T> T mock(Class<T> classToMock) {
        return mock(classToMock, new MockSettings());
    }
    
    /**
     * 【已更新V2】
     * 创建带有自定义设置的mock对象。可通过MockSettings启用静态/私有/构造函数等高级mock。
     * @param <T> 要mock的类型
     * @param classToMock 要mock的类或接口
     * @param settings mock配置
     * @return mock实例
     */
    public static <T> T mock(Class<T> classToMock, MockSettings settings) {
        if (!settings.isEnhancedMockEnabled()) {
            return MockitoAdapter.createMock(classToMock);
        }
        return MockCreator.createMock(classToMock, settings);
    }
    
    /**
     * 【已更新V2】
     * 创建一个新的MockSettings，用于链式配置mock行为（如启用静态/私有方法mock等）。
     * @return MockSettings实例
     */
    public static MockSettings withSettings() {
        return new MockSettings();
    }
    
    /**
     * 【已更新V2】
     * 配置mock对象的某个方法的行为（存根）。
     * <p>
     * 例：Mock.when(mock, "findById", 1L).thenReturn(Optional.of(user));
     * @param <T> 返回值类型
     * @param mock mock对象
     * @param methodName 方法名
     * @param args 方法参数
     * @return MethodInterceptor，可继续链式thenReturn/thenThrow/thenImplement
     */
    public static <T> MethodInterceptor<T> when(T mock, String methodName, Object... args) {
        return new MethodInterceptor<>(mock, methodName, args);
    }
    
    /**
     * 【已更新V2】
     * 验证mock对象的某个方法是否被调用过。支持once/never/times(n)等链式调用。
     * <p>
     * 例：Mock.verify(mock).once().findById(1L);
     * @param <T> mock类型
     * @param mock mock对象
     * @return VerificationBuilder，可继续链式调用
     */
    public static <T> VerificationBuilder<T> verify(T mock) {
        return new VerificationBuilder<>(mock);
    }
    
    /**
     * 【已更新V2】
     * 重置mock对象的所有行为存根和调用记录。仅影响本mock实例，不影响其他mock。
     * @param <T> mock类型
     * @param mock 要重置的mock对象
     */
    public static <T> void reset(T mock) {
        if (mock == null) return;
        if (java.lang.reflect.Proxy.isProxyClass(mock.getClass())) {
            java.lang.reflect.InvocationHandler handler = java.lang.reflect.Proxy.getInvocationHandler(mock);
            if (handler instanceof MockitoAdapter.MockInvocationHandler) {
                MockitoAdapter.MockInvocationHandler h = (MockitoAdapter.MockInvocationHandler) handler;
                h.methodCalls.clear();
                h.stubs.clear();
            }
        } else {
            // enhanced mock: 反射清空 _methodCalls/_methodStubs
            try {
                java.lang.reflect.Field callsField = mock.getClass().getDeclaredField("_methodCalls");
                callsField.setAccessible(true);
                ((java.util.Map) callsField.get(mock)).clear();
                java.lang.reflect.Field stubsField = mock.getClass().getDeclaredField("_methodStubs");
                stubsField.setAccessible(true);
                ((java.util.Map) stubsField.get(mock)).clear();
            } catch (Exception e) { /* ignore */ }
        }
        // 清理全局 ThreadLocal 状态 (已移除)
        // lastCallContext.remove();
        // lastMethodCall.remove();
    }
    
    /**
     * 【已更新V2】
     * 用于链式验证mock方法调用次数的构建器。支持once/never/times(n)等语义。
     * @param <T> mock类型
     */
    public static class VerificationBuilder<T> {
        private final T mock;
        private int expectedTimes = -1; // -1: 不限制，0: never, 1: once, n: times(n)
        private String methodName;
        private Object[] expectedArgs;

        VerificationBuilder(T mock) {
            this.mock = mock;
        }

        /**
         * 期望方法被调用1次。
         * @return 代理mock对象
         */
        public T once() {
            this.expectedTimes = 1;
            return createProxy();
        }
        /**
         * 期望方法从未被调用。
         * @return 代理mock对象
         */
        public T never() {
            this.expectedTimes = 0;
            return createProxy();
        }
        /**
         * 期望方法被调用指定次数。
         * @param times 次数
         * @return 代理mock对象
         */
        public T times(int times) {
            this.expectedTimes = times;
            return createProxy();
        }
        /**
         * 不限制调用次数，仅做存在性验证。
         * @return 代理mock对象
         */
        public T get() {
            this.expectedTimes = -1;
            return createProxy();
        }
        /**
         * 【已更新V2】
         * 创建代理对象，拦截方法调用并检查调用次数。
         * @return 代理mock对象
         */
        @SuppressWarnings("unchecked")
        private T createProxy() {
            return (T) java.lang.reflect.Proxy.newProxyInstance(
                mock.getClass().getClassLoader(),
                mock.getClass().getInterfaces(),
                (proxy, method, args) -> {
                    this.methodName = method.getName();
                    this.expectedArgs = args;
                    // 查找调用记录
                    java.lang.reflect.InvocationHandler handler = java.lang.reflect.Proxy.getInvocationHandler(mock);
                    if (handler instanceof MockitoAdapter.MockInvocationHandler) {
                        MockitoAdapter.MockInvocationHandler h =
                            (MockitoAdapter.MockInvocationHandler) handler;
                        // 构造 key
                        Object key = new MockitoAdapter.MockInvocationHandler.MethodCallKey(method.getName(), args);
                        java.util.List<Object[]> callList = h.methodCalls.get(key);
                        int actual = callList == null ? 0 : callList.size();
                        if (expectedTimes >= 0 && actual != expectedTimes) {
                            throw new AssertionError("方法 " + method.getName() + "(" + java.util.Arrays.toString(args) + ") 期望被调用 " + expectedTimes + " 次，实际 " + actual + " 次");
                        }
                        // 返回默认值
                        return h.getDefaultReturnValue(method.getReturnType());
                    } else {
                        throw new UnsupportedOperationException("只支持 JDK Proxy mock 的 verify");
                    }
                }
            );
        }
    }
} 