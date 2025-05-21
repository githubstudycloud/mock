package com.mocktutorial.advanced.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles interception and processing of static method calls for mocking.
 * This class is used internally by the framework to route static method calls
 * to their appropriate mock implementations.
 */
public class StaticMockHandler {
    private static final Logger logger = LoggerFactory.getLogger(StaticMockHandler.class);
    
    // Map of class name -> method name -> mock behavior
    private static final Map<String, Map<String, Object>> staticMockBehaviors = new ConcurrentHashMap<>();
    
    // Map to store original method byte code for restoration
    private static final Map<String, byte[]> originalClassBytes = new ConcurrentHashMap<>();

    /**
     * Registers a behavior for a static method.
     * 
     * @param className fully qualified class name
     * @param methodName method name
     * @param behavior the behavior to apply (return value, exception, or function)
     */
    public static void registerBehavior(String className, String methodName, Object behavior) {
        Map<String, Object> methodMap = staticMockBehaviors.computeIfAbsent(className, k -> new ConcurrentHashMap<>());
        methodMap.put(methodName, behavior);
        logger.debug("Registered behavior for static method {}.{}", className, methodName);
    }
    
    /**
     * Handles an intercepted static method call.
     * 
     * @param className fully qualified class name
     * @param methodName method name
     * @param args method arguments
     * @return the mock result or null if no behavior was registered
     * @throws Throwable if the registered behavior is an exception
     */
    @SuppressWarnings("unchecked")
    public static Object handleStaticMethodCall(String className, String methodName, Object[] args) throws Throwable {
        Map<String, Object> methodMap = staticMockBehaviors.get(className);
        
        if (methodMap != null && methodMap.containsKey(methodName)) {
            Object behavior = methodMap.get(methodName);
            logger.debug("Handling static method call {}.{}", className, methodName);
            
            if (behavior instanceof Throwable) {
                throw (Throwable) behavior;
            } else if (behavior instanceof Function) {
                return ((Function<Object[], Object>) behavior).apply(args);
            } else {
                return behavior;
            }
        }
        
        logger.debug("No mock behavior found for static method {}.{}", className, methodName);
        return null;  // Default return value if no behavior is defined
    }
    
    /**
     * Stores the original class bytecode for later restoration.
     * 
     * @param className fully qualified class name
     * @param originalBytes the original bytecode
     */
    public static void storeOriginalBytecode(String className, byte[] originalBytes) {
        originalClassBytes.putIfAbsent(className, originalBytes);
    }
    
    /**
     * Retrieves the original bytecode for a class.
     * 
     * @param className fully qualified class name
     * @return the original bytecode or null if not stored
     */
    public static byte[] getOriginalBytecode(String className) {
        return originalClassBytes.get(className);
    }
    
    /**
     * Clears all registered mock behaviors.
     */
    public static void resetAll() {
        staticMockBehaviors.clear();
        logger.info("Reset all static method mocks");
    }
    
    /**
     * Clears mock behaviors for a specific class.
     * 
     * @param className fully qualified class name
     */
    public static void reset(String className) {
        staticMockBehaviors.remove(className);
        logger.info("Reset static method mocks for class: {}", className);
    }
    
    /**
     * Checks if there are any mock behaviors registered for this class.
     * 
     * @param className fully qualified class name
     * @return true if the class has mock behaviors registered
     */
    public static boolean hasMockBehaviors(String className) {
        Map<String, Object> methodMap = staticMockBehaviors.get(className);
        return methodMap != null && !methodMap.isEmpty();
    }
}