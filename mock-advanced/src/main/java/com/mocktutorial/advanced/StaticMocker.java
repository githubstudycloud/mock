package com.mocktutorial.advanced;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides advanced support for mocking static methods.
 */
public class StaticMocker {
    private static final Logger logger = LoggerFactory.getLogger(StaticMocker.class);
    private static final Map<Class<?>, Map<String, Object>> staticMethodReturns = new ConcurrentHashMap<>();
    
    /**
     * Prepares a class for static method mocking.
     * 
     * @param classToMock the class to prepare for static mocking
     */
    public static void prepareForStaticMocking(Class<?> classToMock) {
        try {
            // In a real implementation, this would use bytecode manipulation to
            // modify the class's static methods at runtime
            // For now, we'll just set up the tracking structures
            staticMethodReturns.putIfAbsent(classToMock, new ConcurrentHashMap<>());
            logger.info("Prepared class for static mocking: {}", classToMock.getName());
        } catch (Exception e) {
            logger.error("Failed to prepare class for static mocking: " + classToMock.getName(), e);
            throw new RuntimeException("Failed to prepare for static mocking", e);
        }
    }
    
    /**
     * Configures a static method to return the specified value.
     * 
     * @param <T> the class type
     * @param <R> the return type of the method
     * @param clazz the class containing the static method
     * @param methodName the name of the static method
     * @param returnValue the value to return when the method is called
     */
    public static <T, R> void when(Class<T> clazz, String methodName, R returnValue) {
        Map<String, Object> methodMap = staticMethodReturns.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>());
        methodMap.put(methodName, returnValue);
        logger.debug("Configured static method {} in class {} to return {}", methodName, clazz.getName(), returnValue);
    }
    
    /**
     * Configures a static method to throw the specified exception.
     * 
     * @param <T> the class type
     * @param clazz the class containing the static method
     * @param methodName the name of the static method
     * @param throwable the exception to throw when the method is called
     */
    public static <T> void whenThrow(Class<T> clazz, String methodName, Throwable throwable) {
        Map<String, Object> methodMap = staticMethodReturns.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>());
        methodMap.put(methodName, throwable);
        logger.debug("Configured static method {} in class {} to throw {}", methodName, clazz.getName(), throwable);
    }
    
    /**
     * Configures a static method to use the specified implementation.
     * 
     * @param <T> the class type
     * @param <R> the return type of the method
     * @param clazz the class containing the static method
     * @param methodName the name of the static method
     * @param implementation the function to execute when the method is called
     */
    public static <T, R> void whenImplement(Class<T> clazz, String methodName, Function<Object[], R> implementation) {
        Map<String, Object> methodMap = staticMethodReturns.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>());
        methodMap.put(methodName, implementation);
        logger.debug("Configured static method {} in class {} with custom implementation", methodName, clazz.getName());
    }
    
    /**
     * Handles a static method call.
     * This method would be called from the bytecode-modified static methods.
     * 
     * @param <R> the return type of the method
     * @param clazz the class containing the static method
     * @param methodName the name of the static method
     * @param args the arguments passed to the method
     * @return the configured return value or the result of the original method
     * @throws Throwable if a configured exception is thrown
     */
    @SuppressWarnings("unchecked")
    public static <R> R handleStaticMethodCall(Class<?> clazz, String methodName, Object[] args) throws Throwable {
        Map<String, Object> methodMap = staticMethodReturns.get(clazz);
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
        logger.warn("No mock configuration found for static method {} in class {}", methodName, clazz.getName());
        return null;
    }
    
    /**
     * Resets all static method mocks.
     */
    public static void resetAll() {
        staticMethodReturns.clear();
        logger.info("Reset all static method mocks");
    }
    
    /**
     * Resets static method mocks for the specified class.
     * 
     * @param clazz the class to reset
     */
    public static void reset(Class<?> clazz) {
        staticMethodReturns.remove(clazz);
        logger.info("Reset static method mocks for class: {}", clazz.getName());
    }
} 