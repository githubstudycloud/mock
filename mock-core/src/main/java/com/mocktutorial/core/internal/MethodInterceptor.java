package com.mocktutorial.core.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

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
        
        // 存储方法调用和异常的映射
        if (lastMethodReturnValue != null) {
            methodExceptions.put(lastMethodReturnValue, throwable);
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
        
        // 存储方法调用和实现的映射
        if (lastMethodReturnValue != null) {
            methodImplementations.put(lastMethodReturnValue, implementation);
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
} 