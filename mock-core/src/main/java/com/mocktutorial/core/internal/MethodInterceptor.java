package com.mocktutorial.core.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import com.mocktutorial.core.Mock;

/**
 * Intercepts method calls on mock objects for stubbing and verification.
 * 
 * @param <T> the type of the mock
 */
public class MethodInterceptor<T> {
    private final T mock;
    private Object lastMethodReturnValue;
    private boolean methodCalled = false;
    
    // 存储方法调用和返回值的映射
    private static final Map<Object, Object> methodReturnValues = new HashMap<>();
    private static final Map<Object, Throwable> methodExceptions = new HashMap<>();
    private static final Map<Object, Function<Object[], ?>> methodImplementations = new HashMap<>();
    
    /**
     * Creates a new method interceptor for the given mock.
     * 
     * @param mock the mock object
     */
    public MethodInterceptor(T mock) {
        this.mock = mock;
    }
    
    /**
     * Records the result of a method call for later verification.
     * 
     * @param <R> the return type of the method
     * @param returnValue the value returned by the method
     */
    public <R> void recordMethodCall(R returnValue) {
        this.lastMethodReturnValue = returnValue;
        this.methodCalled = true;
    }
    
    /**
     * Configures a method to return the specified value.
     * 
     * @param <R> the return type of the method
     * @param returnValue the value to return
     * @return a result builder for additional configuration
     */
    @SuppressWarnings("unchecked")
    public <R> ResultBuilder<R> thenReturn(R returnValue) {
        if (!methodCalled) {
            throw new IllegalStateException("No method call recorded. Make sure to call a method on the mock first.");
        }
        // 存储方法调用和返回值的映射
        if (lastMethodReturnValue != null) {
            methodReturnValues.put(lastMethodReturnValue, returnValue);
            Mock.LastCallContext ctx = com.mocktutorial.core.Mock.getLastCallContext();
            if (ctx != null && ctx.mock != null) {
                if (java.lang.reflect.Proxy.isProxyClass(ctx.mock.getClass())) {
                    com.mocktutorial.core.internal.MockitoAdapter.MockInvocationHandler.stubReturn(ctx.mock, ctx.methodName, ctx.args, returnValue);
                } else {
                    // enhanced mock: 反射设置 _methodStubs
                    try {
                        java.lang.reflect.Field stubsField = ctx.mock.getClass().getDeclaredField("_methodStubs");
                        stubsField.setAccessible(true);
                        java.util.Map stubs = (java.util.Map) stubsField.get(ctx.mock);
                        String callKey = ctx.methodName + java.util.Arrays.deepToString(ctx.args);
                        stubs.put(callKey, returnValue);
                    } catch (Exception e) { throw new RuntimeException(e); }
                }
            }
        }
        return new ResultBuilder<>((R) lastMethodReturnValue);
    }
    
    /**
     * Configures a method to throw the specified exception.
     * 
     * @param throwable the exception to throw
     * @return a result builder for additional configuration
     */
    @SuppressWarnings("unchecked")
    public <R> ResultBuilder<R> thenThrow(Throwable throwable) {
        if (!methodCalled) {
            throw new IllegalStateException("No method call recorded. Make sure to call a method on the mock first.");
        }
        if (lastMethodReturnValue != null) {
            methodExceptions.put(lastMethodReturnValue, throwable);
            Mock.LastCallContext ctx = com.mocktutorial.core.Mock.getLastCallContext();
            if (ctx != null && ctx.mock != null) {
                if (java.lang.reflect.Proxy.isProxyClass(ctx.mock.getClass())) {
                    com.mocktutorial.core.internal.MockitoAdapter.MockInvocationHandler.stubThrow(ctx.mock, ctx.methodName, ctx.args, throwable);
                } else {
                    try {
                        java.lang.reflect.Field stubsField = ctx.mock.getClass().getDeclaredField("_methodStubs");
                        stubsField.setAccessible(true);
                        java.util.Map stubs = (java.util.Map) stubsField.get(ctx.mock);
                        String callKey = ctx.methodName + java.util.Arrays.deepToString(ctx.args);
                        stubs.put(callKey, throwable);
                    } catch (Exception e) { throw new RuntimeException(e); }
                }
            }
        }
        return new ResultBuilder<>((R) lastMethodReturnValue);
    }
    
    /**
     * Configures a method to use the specified implementation.
     * 
     * @param <R> the return type of the method
     * @param implementation the function to execute when the method is called
     * @return a result builder for additional configuration
     */
    @SuppressWarnings("unchecked")
    public <R> ResultBuilder<R> thenImplement(Function<Object[], R> implementation) {
        if (!methodCalled) {
            throw new IllegalStateException("No method call recorded. Make sure to call a method on the mock first.");
        }
        if (lastMethodReturnValue != null) {
            methodImplementations.put(lastMethodReturnValue, implementation);
            Mock.LastCallContext ctx = com.mocktutorial.core.Mock.getLastCallContext();
            if (ctx != null && ctx.mock != null && java.lang.reflect.Proxy.isProxyClass(ctx.mock.getClass())) {
                com.mocktutorial.core.internal.MockitoAdapter.MockInvocationHandler.stubImpl(ctx.mock, ctx.methodName, ctx.args, (Function<Object[], Object>) implementation);
            }
        }
        return new ResultBuilder<>((R) lastMethodReturnValue);
    }
    
    /**
     * 检查方法调用是否有配置的返回值
     * 
     * @param methodCall 方法调用的结果
     * @return 是否有配置的返回值
     */
    public static boolean hasReturnValue(Object methodCall) {
        return methodReturnValues.containsKey(methodCall) || 
               methodExceptions.containsKey(methodCall) || 
               methodImplementations.containsKey(methodCall);
    }
    
    /**
     * 获取方法调用的配置返回值
     * 
     * @param methodCall 方法调用的结果
     * @param args 方法调用的参数
     * @return 配置的返回值
     * @throws Throwable 如果配置了异常则抛出
     */
    public static Object getReturnValue(Object methodCall, Object[] args) throws Throwable {
        if (methodExceptions.containsKey(methodCall)) {
            throw methodExceptions.get(methodCall);
        }
        
        if (methodImplementations.containsKey(methodCall)) {
            Function<Object[], ?> implementation = methodImplementations.get(methodCall);
            return implementation.apply(args);
        }
        
        return methodReturnValues.get(methodCall);
    }
    
    /**
     * Builder for configuring method results.
     * 
     * @param <R> the return type of the method
     */
    public static class ResultBuilder<R> {
        private final R methodReturnValue;
        
        ResultBuilder(R methodReturnValue) {
            this.methodReturnValue = methodReturnValue;
        }
        
        /**
         * Gets the original method return value.
         * 
         * @return the method return value
         */
        public R getMethodReturnValue() {
            return methodReturnValue;
        }
    }
    
    public static void clearAllStubs() {
        methodReturnValues.clear();
        methodExceptions.clear();
        methodImplementations.clear();
    }
} 