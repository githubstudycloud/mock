package com.mocktutorial.advanced.agent;

import com.mocktutorial.advanced.internal.Jdk21Optimizer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import javassist.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agent responsible for modifying class bytecode to enable static method mocking.
 * This class uses Javassist to inject code into static methods that redirects
 * calls to the StaticMocker handler.
 */
public class StaticMethodAgent {
    private static final Logger logger = LoggerFactory.getLogger(StaticMethodAgent.class);
    private static final Map<Class<?>, Boolean> modifiedClasses = new ConcurrentHashMap<>();
    
    /**
     * Modifies a class to enable static method mocking.
     * This injects code into all static methods to redirect method calls through
     * the StaticMocker handler.
     *
     * @param clazz the class to modify
     * @return true if the class was successfully modified, false otherwise
     */
    public static boolean modifyClass(Class<?> clazz) {
        if (modifiedClasses.containsKey(clazz)) {
            logger.debug("Class {} is already modified for static method mocking", clazz.getName());
            return modifiedClasses.get(clazz);
        }
        
        // Try JDK21 optimized path first if available
        if (Jdk21Optimizer.isJdk21OrHigher() && Jdk21Optimizer.applyJdk21Optimizations(clazz)) {
            logger.info("Applied JDK21 optimized static method mocking to class {}", clazz.getName());
            modifiedClasses.put(clazz, true);
            return true;
        }
        
        // Fall back to standard Javassist implementation
        try {
            ClassPool classPool = ClassPool.getDefault();
            
            // Add the class path of the target class
            classPool.appendClassPath(new ClassClassPath(clazz));
            
            // Get the CtClass (compile-time class) for modification
            CtClass ctClass = classPool.get(clazz.getName());
            
            if (ctClass.isFrozen()) {
                logger.warn("Class {} is frozen and cannot be modified", clazz.getName());
                modifiedClasses.put(clazz, false);
                return false;
            }
            
            // Process all static methods in the class
            boolean anyMethodModified = false;
            for (CtMethod method : ctClass.getDeclaredMethods()) {
                if (Modifier.isStatic(method.getModifiers()) && !Modifier.isNative(method.getModifiers())) {
                    modifyStaticMethod(ctClass, method);
                    anyMethodModified = true;
                }
            }
            
            if (anyMethodModified) {
                // Replace the original class with our modified version
                ctClass.toClass(clazz.getClassLoader(), clazz.getProtectionDomain());
                logger.info("Successfully modified class {} for static method mocking", clazz.getName());
                modifiedClasses.put(clazz, true);
                return true;
            } else {
                logger.info("No static methods to modify in class {}", clazz.getName());
                modifiedClasses.put(clazz, false);
                return false;
            }
        } catch (Exception e) {
            logger.error("Failed to modify class " + clazz.getName() + " for static method mocking", e);
            modifiedClasses.put(clazz, false);
            return false;
        }
    }
    
    /**
     * Modifies a static method to redirect calls through the StaticMocker handler.
     *
     * @param ctClass the CtClass containing the method
     * @param method the static method to modify
     * @throws CannotCompileException if the method cannot be modified
     * @throws NotFoundException if a class or method cannot be found
     */
    private static void modifyStaticMethod(CtClass ctClass, CtMethod method) 
            throws CannotCompileException, NotFoundException {
        // Create a unique name for the original method
        String originalMethodName = method.getName() + "$original";
        
        // Rename the original method
        method.setName(originalMethodName);
        
        // Create a new method with the original name that delegates to our handler
        CtMethod newMethod = CtNewMethod.copy(method, method.getName(), ctClass, null);
        
        // Build the body for the new method
        StringBuilder body = new StringBuilder();
        body.append("{\n");
        
        // Try to handle the method call through StaticMocker
        body.append("    try {\n");
        body.append("        Object[] args = new Object[]{");
        
        // Add parameters to args array
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            if (i > 0) body.append(", ");
            body.append("$").append(i + 1);
        }
        body.append("};\n");
        
        // Call the StaticMocker handler
        body.append("        Object result = com.mocktutorial.advanced.StaticMocker.handleStaticMethodCall(");
        body.append(ctClass.getName()).append(".class, \"").append(method.getName()).append("\", args);\n");
        
        // Check if the handler processed the call
        body.append("        if (result != com.mocktutorial.advanced.StaticMocker.PROCEED) {\n");
        
        // Handle method return based on return type
        if (!method.getReturnType().equals(CtClass.voidType)) {
            body.append("            return ");
            appendCast(body, method.getReturnType());
            body.append("result;\n");
        }
        body.append("        }\n");
        
        body.append("    } catch (Throwable e) {\n");
        body.append("        if (e instanceof RuntimeException) throw (RuntimeException)e;\n");
        body.append("        if (e instanceof Error) throw (Error)e;\n");
        body.append("        throw new RuntimeException(\"StaticMocker error\", e);\n");
        body.append("    }\n");
        
        // Call the original method if the handler didn't process the call
        body.append("    return ");
        if (method.getReturnType().equals(CtClass.voidType)) {
            body.delete(body.length() - 7, body.length()); // Remove "return "
        }
        
        body.append(originalMethodName).append("(");
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            if (i > 0) body.append(", ");
            body.append("$").append(i + 1);
        }
        body.append(");\n");
        
        body.append("}");
        
        newMethod.setBody(body.toString());
        ctClass.addMethod(newMethod);
        
        logger.debug("Modified static method {}.{} for mocking", ctClass.getName(), method.getName());
    }
    
    /**
     * Appends the appropriate cast based on the method return type.
     *
     * @param body the StringBuilder to append to
     * @param returnType the return type of the method
     * @throws NotFoundException if a class cannot be found
     */
    private static void appendCast(StringBuilder body, CtClass returnType) throws NotFoundException {
        if (returnType.isPrimitive()) {
            if (returnType.equals(CtClass.booleanType)) {
                body.append("((Boolean)result).booleanValue();\n");
            } else if (returnType.equals(CtClass.byteType)) {
                body.append("((Byte)result).byteValue();\n");
            } else if (returnType.equals(CtClass.charType)) {
                body.append("((Character)result).charValue();\n");
            } else if (returnType.equals(CtClass.shortType)) {
                body.append("((Short)result).shortValue();\n");
            } else if (returnType.equals(CtClass.intType)) {
                body.append("((Integer)result).intValue();\n");
            } else if (returnType.equals(CtClass.longType)) {
                body.append("((Long)result).longValue();\n");
            } else if (returnType.equals(CtClass.floatType)) {
                body.append("((Float)result).floatValue();\n");
            } else if (returnType.equals(CtClass.doubleType)) {
                body.append("((Double)result).doubleValue();\n");
            }
        } else {
            body.append("(").append(returnType.getName()).append(")");
        }
    }
    
    /**
     * Resets the agent state.
     * This removes all cached class modification information.
     */
    public static void reset() {
        modifiedClasses.clear();
        logger.info("Reset StaticMethodAgent state");
    }
} 