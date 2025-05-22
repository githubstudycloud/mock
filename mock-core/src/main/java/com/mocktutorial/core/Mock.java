package com.mocktutorial.core;

import com.mocktutorial.core.internal.MockitoAdapter;
import com.mocktutorial.core.internal.MethodInterceptor;
import com.mocktutorial.core.internal.MockCreator;
import com.mocktutorial.core.internal.MockSettings;

/**
 * Main entry point for the enhanced mock framework.
 * This class provides static methods to create and configure mocks.
 */
public class Mock {
    
    /**
     * Creates a mock instance of the given class.
     * 
     * @param <T> the type to mock
     * @param classToMock the class to create a mock of
     * @return a mock instance
     */
    public static <T> T mock(Class<T> classToMock) {
        return mock(classToMock, new MockSettings());
    }
    
    /**
     * Creates a mock instance of the given class with specified settings.
     * 
     * @param <T> the type to mock
     * @param classToMock the class to create a mock of
     * @param settings the mock settings
     * @return a mock instance
     */
    public static <T> T mock(Class<T> classToMock, MockSettings settings) {
        // Default implementation uses Mockito adapter for now
        // This will be replaced with our own implementation
        if (!settings.isEnhancedMockEnabled()) {
            return MockitoAdapter.createMock(classToMock);
        }
        
        // Use our own mock creation logic when enhanced mode is enabled
        return MockCreator.createMock(classToMock, settings);
    }
    
    /**
     * Creates a new settings builder for configuring mocks.
     * 
     * @return a new mock settings instance
     */
    public static MockSettings withSettings() {
        return new MockSettings();
    }
    
    /**
     * Prepares for method stubbing.
     * Use this to define behavior for method calls on mocks.
     * 
     * @param <T> the type of the return value
     * @param mock the mock object
     * @param methodName the name of the method to be stubbed
     * @param args the arguments to the method
     * @return the method interceptor for chaining
     */
    public static <T> MethodInterceptor<T> when(T mock, String methodName, Object... args) {
        return new MethodInterceptor<>(mock, methodName, args);
    }
    
    /**
     * Prepares for method verification.
     * Use this to verify if specific methods were called on mocks.
     * 
     * @param <T> the type of the mock
     * @param mock the mock object
     * @return a verification builder
     */
    public static <T> VerificationBuilder<T> verify(T mock) {
        return new VerificationBuilder<>(mock);
    }
    
    /**
     * Resets a mock to its initial state.
     * 
     * @param <T> the type of the mock
     * @param mock the mock object to reset
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
     * Builder class for verification operations.
     * 
     * @param <T> the type of the mock
     */
    public static class VerificationBuilder<T> {
        private final T mock;
        private int expectedTimes = -1; // -1: 不限制，0: never, 1: once, n: times(n)
        private String methodName;
        private Object[] expectedArgs;

        VerificationBuilder(T mock) {
            this.mock = mock;
        }

        public T once() {
            this.expectedTimes = 1;
            return createProxy();
        }
        public T never() {
            this.expectedTimes = 0;
            return createProxy();
        }
        public T times(int times) {
            this.expectedTimes = times;
            return createProxy();
        }
        public T get() {
            this.expectedTimes = -1;
            return createProxy();
        }
        // 创建代理，拦截方法调用，检查调用次数
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