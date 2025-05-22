package com.mocktutorial.advanced;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mocktutorial.advanced.agent.ConstructorAgent;
import com.mocktutorial.advanced.internal.Jdk21Optimizer;

/**
 * Provides support for mocking constructors.
 * This allows you to intercept constructor calls and either provide
 * a custom implementation or return a pre-created instance.
 */
public class ConstructorMocker {
    private static final Logger logger = LoggerFactory.getLogger(ConstructorMocker.class);
    
    // Store constructor mocks by class and constructor parameter types
    private static final Map<Class<?>, Map<String, Object>> constructorMocks = new ConcurrentHashMap<>();
    
    /**
     * Special value indicating that the original constructor should be called.
     * This is used internally and should not be returned from custom implementations.
     */
    public static final Object PROCEED = new Object();
    
    /**
     * Prepares a class for constructor mocking.
     * 
     * @param <T> the type of the class
     * @param classToMock the class to prepare for constructor mocking
     */
    public static <T> void prepareForConstructorMocking(Class<T> classToMock) {
        try {
            // Set up tracking structures
            constructorMocks.putIfAbsent(classToMock, new ConcurrentHashMap<>());
            
            // Try JDK21 optimized path first if available
            if (Jdk21Optimizer.isJdk21OrHigher() && 
                Jdk21Optimizer.applyJdk21Optimizations(classToMock)) {
                logger.info("Applied JDK21 optimized constructor mocking to class {}", 
                           classToMock.getName());
                return;
            }
            
            // Fall back to standard implementation
            boolean modified = ConstructorAgent.modifyClass(classToMock);
            
            if (modified) {
                logger.info("Prepared class for constructor mocking: {}", classToMock.getName());
            } else {
                logger.warn("Failed to prepare class for constructor mocking: {}", classToMock.getName());
            }
        } catch (Exception e) {
            logger.error("Failed to prepare class for constructor mocking: " + classToMock.getName(), e);
            throw new RuntimeException("Failed to prepare for constructor mocking", e);
        }
    }
    
    /**
     * Configures a constructor to return a pre-created instance.
     * 
     * @param <T> the type of the class
     * @param clazz the class containing the constructor
     * @param instance the instance to return when the constructor is called
     * @param parameterTypes the parameter types of the constructor
     */
    public static <T> void whenConstructor(Class<T> clazz, T instance, Class<?>... parameterTypes) {
        ensurePrepared(clazz);
        String key = constructorKey(parameterTypes);
        Map<String, Object> constructorMap = constructorMocks.get(clazz);
        constructorMap.put(key, instance);
        logger.debug("Configured constructor for class {} with parameters {} to return instance {}", 
                    clazz.getName(), Arrays.toString(parameterTypes), instance);
    }
    
    /**
     * Configures a constructor to use a custom implementation.
     * 
     * @param <T> the type of the class
     * @param clazz the class containing the constructor
     * @param implementation the function to execute when the constructor is called
     * @param parameterTypes the parameter types of the constructor
     */
    public static <T> void whenConstructorImplement(Class<T> clazz, Function<Object[], T> implementation, 
                                                   Class<?>... parameterTypes) {
        ensurePrepared(clazz);
        String key = constructorKey(parameterTypes);
        Map<String, Object> constructorMap = constructorMocks.get(clazz);
        constructorMap.put(key, implementation);
        logger.debug("Configured constructor for class {} with parameters {} to use custom implementation", 
                    clazz.getName(), Arrays.toString(parameterTypes));
    }
    
    /**
     * Configures the default constructor to use a custom implementation.
     * 
     * @param <T> the type of the class
     * @param clazz the class containing the constructor
     * @param implementation the supplier to execute when the constructor is called
     */
    public static <T> void whenDefaultConstructor(Class<T> clazz, Supplier<T> implementation) {
        whenConstructorImplement(clazz, args -> implementation.get());
    }
    
    /**
     * Handles a constructor call.
     * This method is called from the bytecode-modified constructors.
     * 
     * @param <T> the type of the class
     * @param clazz the class being constructed
     * @param args the arguments passed to the constructor
     * @param parameterTypes the parameter types of the constructor
     * @return the configured instance or PROCEED to indicate the original constructor should be called
     * @throws Throwable if an error occurs
     */
    @SuppressWarnings("unchecked")
    public static <T> T handleConstructorCall(Class<T> clazz, Object[] args, Class<?>[] parameterTypes) 
            throws Throwable {
        
        Map<String, Object> constructorMap = constructorMocks.get(clazz);
        if (constructorMap != null) {
            String key = constructorKey(parameterTypes);
            
            if (constructorMap.containsKey(key)) {
                Object result = constructorMap.get(key);
                
                if (result instanceof Function) {
                    return ((Function<Object[], T>) result).apply(args);
                } else {
                    return (T) result;
                }
            }
        }
        
        // If no mock configuration is found, indicate that the original constructor should be called
        logger.debug("No mock configuration found for constructor of class {} with parameters {}, proceeding with original", 
                    clazz.getName(), Arrays.toString(parameterTypes));
        return (T) PROCEED;
    }
    
    /**
     * Creates a key for identifying a constructor based on its parameter types.
     * 
     * @param parameterTypes the parameter types of the constructor
     * @return a string key
     */
    private static String constructorKey(Class<?>... parameterTypes) {
        if (parameterTypes == null || parameterTypes.length == 0) {
            return "()";
        }
        
        StringBuilder key = new StringBuilder("(");
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i > 0) {
                key.append(",");
            }
            key.append(parameterTypes[i].getName());
        }
        key.append(")");
        return key.toString();
    }
    
    /**
     * Ensures that a class is prepared for constructor mocking.
     * 
     * @param clazz the class to prepare
     */
    private static void ensurePrepared(Class<?> clazz) {
        if (!constructorMocks.containsKey(clazz)) {
            prepareForConstructorMocking(clazz);
        }
    }
    
    /**
     * Creates an instance of a class without calling its constructor.
     * 
     * @param <T> the type of the class
     * @param clazz the class to instantiate
     * @return a new instance of the class
     * @throws Exception if instantiation fails
     */
    @SuppressWarnings("unchecked")
    public static <T> T createInstanceWithoutConstructor(Class<T> clazz) throws Exception {
        // 使用Unsafe.allocateInstance确保不调用构造函数
        try {
            java.lang.reflect.Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);
            return (T) unsafe.allocateInstance(clazz);
        } catch (Exception e) {
            logger.error("Failed to create instance without constructor for class " + clazz.getName(), e);
            throw new RuntimeException("Failed to create instance without constructor for class " + clazz.getName(), e);
        }
    }
    
    /**
     * Resets all constructor mocks.
     */
    public static void resetAll() {
        constructorMocks.clear();
        logger.info("Reset all constructor mocks");
    }
    
    /**
     * Resets constructor mocks for the specified class.
     * 
     * @param clazz the class to reset
     */
    public static void reset(Class<?> clazz) {
        constructorMocks.remove(clazz);
        logger.info("Reset constructor mocks for class: {}", clazz.getName());
    }
} 