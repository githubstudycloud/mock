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
    private final String methodName;
    private final Object[] args;
    
    /**
     * Creates a new method interceptor for the given mock.
     * 
     * @param mock the mock object
     */
    public MethodInterceptor(T mock, String methodName, Object[] args) {
        this.mock = mock;
        this.methodName = methodName;
        this.args = args == null ? new Object[0] : args.clone();
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
        if (mock != null) {
            if (java.lang.reflect.Proxy.isProxyClass(mock.getClass())) {
                com.mocktutorial.core.internal.MockitoAdapter.MockInvocationHandler.stubReturn(mock, methodName, args, returnValue);
            } else {
                try {
                    java.lang.reflect.Field stubsField = mock.getClass().getDeclaredField("_methodStubs");
                    stubsField.setAccessible(true);
                    java.util.Map stubs = (java.util.Map) stubsField.get(mock);
                    String callKey = methodName + java.util.Arrays.deepToString(args);
                    stubs.put(callKey, returnValue);
                } catch (Exception e) { throw new RuntimeException(e); }
            }
        }
        return new ResultBuilder<>(null);
    }
    
    /**
     * Configures a method to throw the specified exception.
     * 
     * @param throwable the exception to throw
     * @return a result builder for additional configuration
     */
    @SuppressWarnings("unchecked")
    public <R> ResultBuilder<R> thenThrow(Throwable throwable) {
        if (mock != null) {
            if (java.lang.reflect.Proxy.isProxyClass(mock.getClass())) {
                com.mocktutorial.core.internal.MockitoAdapter.MockInvocationHandler.stubThrow(mock, methodName, args, throwable);
            } else {
                try {
                    java.lang.reflect.Field stubsField = mock.getClass().getDeclaredField("_methodStubs");
                    stubsField.setAccessible(true);
                    java.util.Map stubs = (java.util.Map) stubsField.get(mock);
                    String callKey = methodName + java.util.Arrays.deepToString(args);
                    stubs.put(callKey, throwable);
                } catch (Exception e) { throw new RuntimeException(e); }
            }
        }
        return new ResultBuilder<>(null);
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
        if (mock != null) {
            if (java.lang.reflect.Proxy.isProxyClass(mock.getClass())) {
                com.mocktutorial.core.internal.MockitoAdapter.MockInvocationHandler.stubImpl(mock, methodName, args, (Function<Object[], Object>) implementation);
            } else {
                try {
                    java.lang.reflect.Field stubsField = mock.getClass().getDeclaredField("_methodStubs");
                    stubsField.setAccessible(true);
                    java.util.Map stubs = (java.util.Map) stubsField.get(mock);
                    String callKey = methodName + java.util.Arrays.deepToString(args);
                    stubs.put(callKey, implementation);
                } catch (Exception e) { throw new RuntimeException(e); }
            }
        }
        return new ResultBuilder<>(null);
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