package com.mocktutorial.advanced;

import com.mocktutorial.advanced.internal.StaticBytecodeEnhancer;
import com.mocktutorial.advanced.internal.StaticMockHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

/**
 * Builder for creating and configuring static method mocks.
 * This class provides a fluent API for mocking static methods.
 */
public class StaticMockBuilder {
    private static final Logger logger = LoggerFactory.getLogger(StaticMockBuilder.class);
    private static final StaticBytecodeEnhancer enhancer = new StaticBytecodeEnhancer();
    
    private final Class<?> targetClass;
    private final String methodName;
    
    /**
     * Creates a new static mock builder for the specified class and method.
     * 
     * @param targetClass the class containing the static method
     * @param methodName the name of the static method
     */
    private StaticMockBuilder(Class<?> targetClass, String methodName) {
        this.targetClass = targetClass;
        this.methodName = methodName;
    }
    
    /**
     * Creates a new static mock builder for the specified class.
     * 
     * @param targetClass the class containing static methods to mock
     * @return a new static mock builder
     */
    public static StaticMockBuilder forClass(Class<?> targetClass) {
        try {
            // Enable static mocking for the class
            enableStaticMockingFor(targetClass);
            return new StaticMockBuilder(targetClass, null);
        } catch (Exception e) {
            logger.error("Failed to create static mock for class: " + targetClass.getName(), e);
            throw new RuntimeException("Failed to create static mock", e);
        }
    }
    
    /**
     * Specifies the static method to mock.
     * 
     * @param methodName the name of the static method
     * @return a new static mock builder focused on the specified method
     */
    public StaticMockBuilder method(String methodName) {
        return new StaticMockBuilder(this.targetClass, methodName);
    }
    
    /**
     * Configures the static method to return the specified value.
     * 
     * @param <R> the return type of the method
     * @param returnValue the value to return
     * @return this builder for chaining
     */
    public <R> StaticMockBuilder thenReturn(R returnValue) {
        if (methodName == null) {
            throw new IllegalStateException("Method name must be specified using method()");
        }
        
        StaticMockHandler.registerBehavior(targetClass.getName(), methodName, returnValue);
        return this;
    }
    
    /**
     * Configures the static method to throw the specified exception.
     * 
     * @param throwable the exception to throw
     * @return this builder for chaining
     */
    public StaticMockBuilder thenThrow(Throwable throwable) {
        if (methodName == null) {
            throw new IllegalStateException("Method name must be specified using method()");
        }
        
        StaticMockHandler.registerBehavior(targetClass.getName(), methodName, throwable);
        return this;
    }
    
    /**
     * Configures the static method to execute the specified implementation.
     * 
     * @param <R> the return type of the method
     * @param implementation the function to execute
     * @return this builder for chaining
     */
    public <R> StaticMockBuilder thenImplement(Function<Object[], R> implementation) {
        if (methodName == null) {
            throw new IllegalStateException("Method name must be specified using method()");
        }
        
        StaticMockHandler.registerBehavior(targetClass.getName(), methodName, implementation);
        return this;
    }
    
    /**
     * Enables static mocking for the specified class.
     * 
     * @param classToMock the class to enable static mocking for
     * @throws Exception if enabling static mocking fails
     */
    public static void enableStaticMockingFor(Class<?> classToMock) throws Exception {
        String className = classToMock.getName();
        
        // If already enabled, do nothing
        if (StaticMockHandler.hasMockBehaviors(className)) {
            logger.debug("Static mocking already enabled for class: {}", className);
            return;
        }
        
        // Modify the class bytecode
        byte[] modifiedBytecode = enhancer.modifyAllStaticMethods(className);
        
        // Redefine the class with the modified bytecode
        enhancer.redefineClass(classToMock, modifiedBytecode);
        
        logger.info("Enabled static mocking for class: {}", className);
    }
    
    /**
     * Resets all static mocks.
     */
    public static void resetAll() {
        StaticMockHandler.resetAll();
    }
    
    /**
     * Resets static mocks for the specified class.
     * 
     * @param classToReset the class to reset
     */
    public static void reset(Class<?> classToReset) {
        StaticMockHandler.reset(classToReset.getName());
    }
    
    /**
     * Restores the original bytecode for the specified class.
     * 
     * @param classToRestore the class to restore
     * @throws Exception if restoration fails
     */
    public static void restore(Class<?> classToRestore) throws Exception {
        enhancer.restoreClass(classToRestore.getName());
        StaticMockHandler.reset(classToRestore.getName());
    }
}