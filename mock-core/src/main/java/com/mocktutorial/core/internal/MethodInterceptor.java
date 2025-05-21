package com.mocktutorial.core.internal;

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
        
        // Set up the mock to return the specified value
        // This will be implemented later with actual bytecode manipulation
        
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
        
        // Set up the mock to throw the specified exception
        // This will be implemented later with actual bytecode manipulation
        
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
        
        // Set up the mock to use the specified implementation
        // This will be implemented later with actual bytecode manipulation
        
        return new ResultBuilder<>((R) lastMethodReturnValue);
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