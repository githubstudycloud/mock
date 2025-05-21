package com.mocktutorial.advanced.internal;

import javassist.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.util.Arrays;

/**
 * Enhanced bytecode manipulator specifically designed for modifying static methods.
 * This class uses Javassist to alter the bytecode of classes to enable static method mocking.
 */
public class StaticBytecodeEnhancer {
    private static final Logger logger = LoggerFactory.getLogger(StaticBytecodeEnhancer.class);
    private static Instrumentation instrumentation;
    private final ClassPool classPool;
    
    /**
     * Creates a new StaticBytecodeEnhancer.
     */
    public StaticBytecodeEnhancer() {
        this.classPool = ClassPool.getDefault();
        // Add the class loader path to ensure all required classes can be found
        this.classPool.appendClassPath(new LoaderClassPath(getClass().getClassLoader()));
    }
    
    /**
     * Sets the instrumentation instance (called from agent premain).
     * 
     * @param inst instrumentation instance
     */
    public static void setInstrumentation(Instrumentation inst) {
        instrumentation = inst;
    }
    
    /**
     * Modifies all static methods in a class to delegate to the StaticMockHandler.
     * 
     * @param className fully qualified class name
     * @return the modified class bytecode
     * @throws Exception if modification fails
     */
    public byte[] modifyAllStaticMethods(String className) throws Exception {
        CtClass ctClass = classPool.get(className);
        
        // Store original bytecode if not already stored
        byte[] originalBytes = ctClass.toBytecode();
        StaticMockHandler.storeOriginalBytecode(className, originalBytes);
        
        // Get all methods from the class
        CtMethod[] methods = ctClass.getDeclaredMethods();
        
        for (CtMethod method : methods) {
            if (Modifier.isStatic(method.getModifiers()) && !Modifier.isNative(method.getModifiers())) {
                modifyStaticMethod(ctClass, method);
            }
        }
        
        byte[] modifiedBytes = ctClass.toBytecode();
        ctClass.detach(); // Release resources
        return modifiedBytes;
    }
    
    /**
     * Modifies a specific static method to delegate to the StaticMockHandler.
     * 
     * @param ctClass the class containing the method
     * @param method the method to modify
     * @throws Exception if modification fails
     */
    private void modifyStaticMethod(CtClass ctClass, CtMethod method) throws Exception {
        String methodName = method.getName();
        CtClass returnType = method.getReturnType();
        
        // Create a new method that will hold the original implementation
        String originalMethodName = methodName + "$original";
        CtMethod originalMethod = CtNewMethod.copy(method, originalMethodName, ctClass, null);
        
        // Make the original method private
        originalMethod.setModifiers(Modifier.PRIVATE | Modifier.STATIC);
        ctClass.addMethod(originalMethod);
        
        // Modify the original method to delegate to StaticMockHandler
        StringBuilder body = new StringBuilder();
        body.append("{\n");
        
        // Prepare method parameters for delegation
        body.append("    Object[] args = new Object[" + method.getParameterTypes().length + "];\n");
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            body.append("    args[" + i + "] = ($w)$" + (i + 1) + ";\n");
        }
        
        // Call the handler
        body.append("    Object result = com.mocktutorial.advanced.internal.StaticMockHandler.handleStaticMethodCall(\"")
            .append(ctClass.getName())
            .append("\", \"")
            .append(methodName)
            .append("\", args);\n");
        
        // Check if we should call the original method
        body.append("    if (result == null) {\n");
        body.append("        // No mock behavior defined, call original method\n");
        
        // Call the original method
        if (!returnType.equals(CtClass.voidType)) {
            body.append("        return ");
        } else {
            body.append("        ");
        }
        
        body.append(originalMethodName).append("(");
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            if (i > 0) body.append(", ");
            body.append("$").append(i + 1);
        }
        body.append(");\n");
        
        if (returnType.equals(CtClass.voidType)) {
            body.append("        return;\n");
        }
        
        body.append("    }\n");
        
        // Return the result from the handler
        if (!returnType.equals(CtClass.voidType)) {
            if (returnType.isPrimitive()) {
                if (returnType.equals(CtClass.booleanType)) {
                    body.append("    return ((Boolean)result).booleanValue();\n");
                } else if (returnType.equals(CtClass.byteType)) {
                    body.append("    return ((Byte)result).byteValue();\n");
                } else if (returnType.equals(CtClass.charType)) {
                    body.append("    return ((Character)result).charValue();\n");
                } else if (returnType.equals(CtClass.shortType)) {
                    body.append("    return ((Short)result).shortValue();\n");
                } else if (returnType.equals(CtClass.intType)) {
                    body.append("    return ((Integer)result).intValue();\n");
                } else if (returnType.equals(CtClass.longType)) {
                    body.append("    return ((Long)result).longValue();\n");
                } else if (returnType.equals(CtClass.floatType)) {
                    body.append("    return ((Float)result).floatValue();\n");
                } else if (returnType.equals(CtClass.doubleType)) {
                    body.append("    return ((Double)result).doubleValue();\n");
                }
            } else {
                body.append("    return (").append(returnType.getName()).append(")result;\n");
            }
        }
        
        body.append("}");
        
        method.setBody(body.toString());
        logger.debug("Modified static method: {}.{}", ctClass.getName(), methodName);
    }
    
    /**
     * Redefines a class with modified bytecode using instrumentation.
     * 
     * @param clazz the class to redefine
     * @param newBytecode the new bytecode
     * @throws Exception if redefinition fails
     */
    public void redefineClass(Class<?> clazz, byte[] newBytecode) throws Exception {
        if (instrumentation == null) {
            throw new IllegalStateException("Instrumentation not available. Java agent not loaded.");
        }
        
        ClassDefinition definition = new ClassDefinition(clazz, newBytecode);
        instrumentation.redefineClasses(definition);
        logger.info("Redefined class: {}", clazz.getName());
    }
    
    /**
     * Restores a class to its original bytecode.
     * 
     * @param className fully qualified class name
     * @throws Exception if restoration fails
     */
    public void restoreClass(String className) throws Exception {
        byte[] originalBytes = StaticMockHandler.getOriginalBytecode(className);
        if (originalBytes == null) {
            throw new IllegalStateException("No original bytecode found for class: " + className);
        }
        
        Class<?> clazz = Class.forName(className);
        redefineClass(clazz, originalBytes);
        logger.info("Restored class to original bytecode: {}", className);
    }
}