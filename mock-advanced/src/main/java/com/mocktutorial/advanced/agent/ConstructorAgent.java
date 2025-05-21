package com.mocktutorial.advanced.agent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import javassist.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agent responsible for modifying class bytecode to enable constructor mocking.
 * This class uses Javassist to inject code into constructors that redirects
 * calls to the ConstructorMocker handler.
 */
public class ConstructorAgent {
    private static final Logger logger = LoggerFactory.getLogger(ConstructorAgent.class);
    private static final Map<Class<?>, Boolean> modifiedClasses = new ConcurrentHashMap<>();
    
    /**
     * Modifies a class to enable constructor mocking.
     * This injects code into all constructors to redirect calls through
     * the ConstructorMocker handler.
     *
     * @param clazz the class to modify
     * @return true if the class was successfully modified, false otherwise
     */
    public static boolean modifyClass(Class<?> clazz) {
        if (modifiedClasses.containsKey(clazz)) {
            logger.debug("Class {} is already modified for constructor mocking", clazz.getName());
            return modifiedClasses.get(clazz);
        }
        
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
            
            // Process all constructors in the class
            boolean anyConstructorModified = false;
            for (CtConstructor constructor : ctClass.getDeclaredConstructors()) {
                if (!Modifier.isPrivate(constructor.getModifiers())) {
                    modifyConstructor(ctClass, constructor);
                    anyConstructorModified = true;
                }
            }
            
            if (anyConstructorModified) {
                // Replace the original class with our modified version
                ctClass.toClass(clazz.getClassLoader(), clazz.getProtectionDomain());
                logger.info("Successfully modified class {} for constructor mocking", clazz.getName());
                modifiedClasses.put(clazz, true);
                return true;
            } else {
                logger.info("No constructors to modify in class {}", clazz.getName());
                modifiedClasses.put(clazz, false);
                return false;
            }
        } catch (Exception e) {
            logger.error("Failed to modify class " + clazz.getName() + " for constructor mocking", e);
            modifiedClasses.put(clazz, false);
            return false;
        }
    }
    
    /**
     * Modifies a constructor to redirect calls through the ConstructorMocker handler.
     *
     * @param ctClass the CtClass containing the constructor
     * @param constructor the constructor to modify
     * @throws CannotCompileException if the constructor cannot be modified
     */
    private static void modifyConstructor(CtClass ctClass, CtConstructor constructor) throws CannotCompileException, NotFoundException {
        // Get the parameter types of the constructor
        CtClass[] parameterTypes = constructor.getParameterTypes();
        
        // Build the parameter types array string
        StringBuilder paramTypesArray = new StringBuilder("new Class[] {");
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i > 0) paramTypesArray.append(", ");
            paramTypesArray.append(parameterTypes[i].getName()).append(".class");
        }
        paramTypesArray.append("}");
        
        // Build the constructor body
        StringBuilder body = new StringBuilder();
        body.append("{\n");
        
        // Try to handle the constructor call through ConstructorMocker
        body.append("    try {\n");
        body.append("        Object[] args = new Object[]{");
        
        // Add parameters to args array
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i > 0) body.append(", ");
            body.append("$").append(i + 1);
        }
        body.append("};\n");
        
        // Call the ConstructorMocker handler
        body.append("        Object mockResult = com.mocktutorial.advanced.ConstructorMocker.handleConstructorCall(");
        body.append(ctClass.getName()).append(".class, args, ").append(paramTypesArray).append(");\n");
        
        // Check if the handler processed the call
        body.append("        if (mockResult != com.mocktutorial.advanced.ConstructorMocker.PROCEED) {\n");
        body.append("            // If a mock result was provided, we need to copy its state to this instance\n");
        body.append("            // In a full implementation, we would use reflection to copy all fields\n");
        body.append("            // For now, this is a placeholder for the actual implementation\n");
        body.append("            System.out.println(\"Constructor mocking not fully implemented yet\");\n");
        body.append("            return;\n");
        body.append("        }\n");
        
        body.append("    } catch (Throwable e) {\n");
        body.append("        if (e instanceof RuntimeException) throw (RuntimeException)e;\n");
        body.append("        if (e instanceof Error) throw (Error)e;\n");
        body.append("        throw new RuntimeException(\"ConstructorMocker error\", e);\n");
        body.append("    }\n");
        
        // Call the original constructor implementation
        if (parameterTypes.length > 0) {
            body.append("    this(");
            for (int i = 0; i < parameterTypes.length; i++) {
                if (i > 0) body.append(", ");
                body.append("$").append(i + 1);
            }
            body.append(");\n");
        } else {
            // For the default constructor, we need different handling
            // This is a placeholder and would need to be properly implemented
            body.append("    // Original constructor logic would be called here\n");
        }
        
        body.append("}");
        
        // Set the constructor body
        constructor.setBody(body.toString());
        
        logger.debug("Modified constructor {} in class {}", constructor.getSignature(), ctClass.getName());
    }
    
    /**
     * Resets the agent state.
     * This removes all cached class modification information.
     */
    public static void reset() {
        modifiedClasses.clear();
        logger.info("Reset ConstructorAgent state");
    }
} 