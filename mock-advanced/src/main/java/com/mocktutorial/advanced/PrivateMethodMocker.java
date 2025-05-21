package com.mocktutorial.advanced;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides support for mocking private methods.
 */
public class PrivateMethodMocker {
    private static final Logger logger = LoggerFactory.getLogger(PrivateMethodMocker.class);
    private static final Map<Object, Map<String, Object>> privateMethodReturns = new ConcurrentHashMap<>();
    
    /**
     * Configures a private method to return the specified value.
     * 
     * @param <T> the object type
     * @param <R> the return type of the method
     * @param mockInstance the mock instance
     * @param methodName the name of the private method
     * @param returnValue the value to return when the method is called
     */
    public static <T, R> void when(T mockInstance, String methodName, R returnValue) {
        Map<String, Object> methodMap = privateMethodReturns.computeIfAbsent(mockInstance, k -> new ConcurrentHashMap<>());
        methodMap.put(methodName, returnValue);
        logger.debug("Configured private method {} in instance {} to return {}", methodName, mockInstance, returnValue);
    }
    
    /**
     * Configures a private method to throw the specified exception.
     * 
     * @param <T> the object type
     * @param mockInstance the mock instance
     * @param methodName the name of the private method
     * @param throwable the exception to throw when the method is called
     */
    public static <T> void whenThrow(T mockInstance, String methodName, Throwable throwable) {
        Map<String, Object> methodMap = privateMethodReturns.computeIfAbsent(mockInstance, k -> new ConcurrentHashMap<>());
        methodMap.put(methodName, throwable);
        logger.debug("Configured private method {} in instance {} to throw {}", methodName, mockInstance, throwable);
    }
    
    /**
     * Configures a private method to use the specified implementation.
     * 
     * @param <T> the object type
     * @param <R> the return type of the method
     * @param mockInstance the mock instance
     * @param methodName the name of the private method
     * @param implementation the function to execute when the method is called
     */
    public static <T, R> void whenImplement(T mockInstance, String methodName, Function<Object[], R> implementation) {
        Map<String, Object> methodMap = privateMethodReturns.computeIfAbsent(mockInstance, k -> new ConcurrentHashMap<>());
        methodMap.put(methodName, implementation);
        logger.debug("Configured private method {} in instance {} with custom implementation", methodName, mockInstance);
    }
    
    /**
     * Handles a private method call.
     * This method would be called from the bytecode-modified private methods.
     * 
     * @param <R> the return type of the method
     * @param mockInstance the mock instance
     * @param methodName the name of the private method
     * @param args the arguments passed to the method
     * @return the configured return value or the result of the original method
     * @throws Throwable if a configured exception is thrown
     */
    @SuppressWarnings("unchecked")
    public static <R> R handlePrivateMethodCall(Object mockInstance, String methodName, Object[] args) throws Throwable {
        Map<String, Object> methodMap = privateMethodReturns.get(mockInstance);
        if (methodMap != null && methodMap.containsKey(methodName)) {
            Object result = methodMap.get(methodName);
            
            if (result instanceof Throwable) {
                throw (Throwable) result;
            } else if (result instanceof Function) {
                return ((Function<Object[], R>) result).apply(args);
            } else {
                return (R) result;
            }
        }
        
        // If no mock configuration is found, we would normally call the original method
        // In a real implementation, this would be handled by bytecode manipulation
        logger.warn("No mock configuration found for private method {} in instance {}", methodName, mockInstance);
        return null;
    }
    
    /**
     * Executes a private method on the specified instance.
     * This is a convenience method for invoking private methods during testing.
     * 
     * @param <T> the instance type
     * @param <R> the return type of the method
     * @param instance the object instance
     * @param methodName the name of the private method
     * @param parameterTypes the parameter types of the method
     * @param args the arguments to pass to the method
     * @return the result of the method invocation
     * @throws Exception if the method cannot be invoked
     */
    @SuppressWarnings("unchecked")
    public static <T, R> R invokePrivateMethod(T instance, String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        try {
            Method method = instance.getClass().getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return (R) method.invoke(instance, args);
        } catch (Exception e) {
            logger.error("Failed to invoke private method: " + methodName, e);
            throw e;
        }
    }
    
    /**
     * Resets all private method mocks.
     */
    public static void resetAll() {
        privateMethodReturns.clear();
        logger.info("Reset all private method mocks");
    }
    
    /**
     * Resets private method mocks for the specified instance.
     * 
     * @param instance the instance to reset
     */
    public static void reset(Object instance) {
        privateMethodReturns.remove(instance);
        logger.info("Reset private method mocks for instance: {}", instance);
    }
} 