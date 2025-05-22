package com.mocktutorial.core.internal;

import javassist.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Creates mock objects using bytecode manipulation.
 */
public class MockCreator {
    private static final Logger logger = LoggerFactory.getLogger(MockCreator.class);
    private static final AtomicLong mockCounter = new AtomicLong(0);
    private static final Map<Class<?>, Class<?>> enhancedClasses = new HashMap<>();
    
    /**
     * Creates a mock instance of the given class with the specified settings.
     * 
     * @param <T> the type to mock
     * @param classToMock the class to create a mock of
     * @param settings the mock settings
     * @return a mock instance
     */
    @SuppressWarnings("unchecked")
    public static <T> T createMock(Class<T> classToMock, MockSettings settings) {
        try {
            if (settings.isMockStaticMethodsEnabled()) {
                return createStaticMock(classToMock, settings);
            } else {
                return createInstanceMock(classToMock, settings);
            }
        } catch (Exception e) {
            logger.error("Failed to create mock for class: " + classToMock.getName(), e);
            // Fall back to default mock behavior
            return MockitoAdapter.createMock(classToMock);
        }
    }
    
    /**
     * Creates a mock instance for regular class mocking.
     * 
     * @param <T> the type to mock
     * @param classToMock the class to create a mock of
     * @param settings the mock settings
     * @return a mock instance
     * @throws Exception if mock creation fails
     */
    @SuppressWarnings("unchecked")
    private static <T> T createInstanceMock(Class<T> classToMock, MockSettings settings) throws Exception {
        // If we already have an enhanced class, use it
        if (enhancedClasses.containsKey(classToMock)) {
            Class<?> enhancedClass = enhancedClasses.get(classToMock);
            return (T) createInstance(enhancedClass);
        }
        
        // Create a new enhanced class
        ClassPool classPool = ClassPool.getDefault();
        
        if (settings.getClassLoader() != null) {
            classPool.appendClassPath(new LoaderClassPath(settings.getClassLoader()));
        }
        
        CtClass originalClass = classPool.get(classToMock.getName());
        String enhancedClassName = classToMock.getName() + "$EnhancedMock$" + mockCounter.getAndIncrement();
        
        CtClass enhancedClass = classPool.makeClass(enhancedClassName);
        
        if (classToMock.isInterface()) {
            enhancedClass.addInterface(originalClass);
        } else {
            enhancedClass.setSuperclass(originalClass);
        }
        
        // Add mock tracking fields
        enhancedClass.addField(CtField.make("private static final java.util.Map _methodCalls = new java.util.HashMap();", enhancedClass));
        enhancedClass.addField(CtField.make("private static final java.util.Map _methodReturns = new java.util.HashMap();", enhancedClass));
        enhancedClass.addField(CtField.make("private static final java.util.Map _methodStubs = new java.util.HashMap();", enhancedClass));
        
        // Override methods based on settings
        overrideMethods(enhancedClass, originalClass, settings);
        
        // Create the enhanced class
        Class<?> resultClass = enhancedClass.toClass();
        enhancedClasses.put(classToMock, resultClass);
        
        // Create an instance of the enhanced class
        return (T) createInstance(resultClass);
    }
    
    /**
     * Creates a mock for static method mocking.
     * 
     * @param <T> the type to mock
     * @param classToMock the class to create a mock of
     * @param settings the mock settings
     * @return a mock instance (placeholder for static mocking)
     * @throws Exception if mock creation fails
     */
    @SuppressWarnings("unchecked")
    private static <T> T createStaticMock(Class<T> classToMock, MockSettings settings) throws Exception {
        // For static mocking, we need to modify the actual class
        // This will be implemented using more advanced bytecode manipulation
        
        // For now, we'll just return a placeholder instance
        return (T) createInstance(classToMock);
    }
    
    /**
     * Overrides methods based on the specified settings.
     * 
     * @param enhancedClass the enhanced class being created
     * @param originalClass the original class being mocked
     * @param settings the mock settings
     * @throws Exception if method overriding fails
     */
    private static void overrideMethods(CtClass enhancedClass, CtClass originalClass, MockSettings settings) throws Exception {
        // Get all methods from the original class
        CtMethod[] methods = originalClass.getDeclaredMethods();
        
        for (CtMethod method : methods) {
            // Skip methods that can't be overridden
            if (Modifier.isPrivate(method.getModifiers()) && !settings.isMockPrivateMethodsEnabled()) {
                continue;
            }
            
            if (Modifier.isFinal(method.getModifiers()) && !settings.isMockFinalMethodsEnabled()) {
                continue;
            }
            
            if (Modifier.isStatic(method.getModifiers()) && !settings.isMockStaticMethodsEnabled()) {
                continue;
            }
            
            // Create a new method that overrides the original
            CtMethod newMethod = CtNewMethod.copy(method, enhancedClass, null);
            
            // Set the method body to delegate to the mock handler
            StringBuilder body = new StringBuilder();
            body.append("{\n");
            
            // Track method calls
            body.append("String methodKey = \"").append(method.getName()).append("\";\n");
            body.append("Object[] args = $args;\n");
            body.append("String callKey = methodKey + java.util.Arrays.deepToString(args);\n");
            body.append("java.util.List callList = (java.util.List) _methodCalls.get(callKey);\n");
            body.append("if (callList == null) { callList = new java.util.ArrayList(); _methodCalls.put(callKey, callList); }\n");
            body.append("callList.add(args);\n");
            
            // Check if this method has a stubbed return value
            body.append("if (_methodStubs.containsKey(callKey)) {\n");
            body.append("    Object stub = _methodStubs.get(callKey);\n");
            body.append("    if (stub instanceof java.lang.Throwable) throw (java.lang.Throwable) stub;\n");
            
            // Handle primitive return types
            if (!method.getReturnType().equals(CtClass.voidType)) {
                body.append("    return (" + method.getReturnType().getName() + ") stub;\n");
            } else {
                body.append("    return;\n");
            }
            
            body.append("}\n");
            
            // If no stubbed value, return default
            if (!method.getReturnType().equals(CtClass.voidType)) {
                body.append("return ");
                
                if (method.getReturnType().isPrimitive()) {
                    if (method.getReturnType().equals(CtClass.booleanType)) {
                        body.append("false");
                    } else if (method.getReturnType().equals(CtClass.charType)) {
                        body.append("(char) 0");
                    } else if (method.getReturnType().equals(CtClass.byteType) 
                            || method.getReturnType().equals(CtClass.shortType) 
                            || method.getReturnType().equals(CtClass.intType) 
                            || method.getReturnType().equals(CtClass.longType)) {
                        body.append("0");
                    } else if (method.getReturnType().equals(CtClass.floatType)) {
                        body.append("0.0f");
                    } else if (method.getReturnType().equals(CtClass.doubleType)) {
                        body.append("0.0d");
                    }
                } else {
                    body.append("null");
                }
                
                body.append(";\n");
            }
            
            body.append("}");
            
            newMethod.setBody(body.toString());
            enhancedClass.addMethod(newMethod);
        }
    }
    
    /**
     * Creates an instance of the specified class.
     * 
     * @param clazz the class to instantiate
     * @return an instance of the class
     * @throws Exception if instantiation fails
     */
    private static Object createInstance(Class<?> clazz) throws Exception {
        try {
            // Try to get a no-arg constructor
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (NoSuchMethodException e) {
            // If no no-arg constructor exists, try to create without calling constructors
            // This requires more advanced bytecode manipulation
            // For now, we'll just return null
            return null;
        }
    }
} 