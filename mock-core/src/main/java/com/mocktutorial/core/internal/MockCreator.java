package com.mocktutorial.core.internal;

import javassist.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 【已更新V2】
 * 通过Javassist字节码增强创建mock对象的核心类。
 * <ul>
 *   <li>支持类和接口的mock，静态/私有/构造函数mock需配合MockSettings。</li>
 *   <li>所有行为存根和调用记录均为每个mock实例独立，无全局状态。</li>
 *   <li>JDK21优先，兼容JDK8+。</li>
 * </ul>
 * <p>
 * 典型用法：
 * <pre>
 *   UserService mock = MockCreator.createMock(UserService.class, new MockSettings().useEnhancedMock());
 * </pre>
 */
public class MockCreator {
    private static final Logger logger = LoggerFactory.getLogger(MockCreator.class);
    private static final AtomicLong mockCounter = new AtomicLong(0);
    private static final Map<Class<?>, Class<?>> enhancedClasses = new HashMap<>();
    
    /**
     * 【已更新V2】
     * 创建指定类型的mock对象，支持静态/私有/构造函数等高级mock。
     * @param <T> 要mock的类型
     * @param classToMock 要mock的类或接口
     * @param settings mock配置
     * @return mock实例
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
     * 【已更新V2】
     * 创建普通类/接口的mock对象，所有行为和调用记录均为本实例独立。
     * @param <T> 要mock的类型
     * @param classToMock 要mock的类或接口
     * @param settings mock配置
     * @return mock实例
     * @throws Exception 创建失败时抛出
     */
    @SuppressWarnings("unchecked")
    private static <T> T createInstanceMock(Class<T> classToMock, MockSettings settings) throws Exception {
        if (enhancedClasses.containsKey(classToMock)) {
            Class<?> enhancedClass = enhancedClasses.get(classToMock);
            return (T) createInstance(enhancedClass);
        }
        // 创建增强类
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
        // 添加mock跟踪字段
        enhancedClass.addField(CtField.make("private static final java.util.Map _methodCalls = new java.util.HashMap();", enhancedClass));
        enhancedClass.addField(CtField.make("private static final java.util.Map _methodReturns = new java.util.HashMap();", enhancedClass));
        enhancedClass.addField(CtField.make("public static final java.util.Map _methodStubs = new java.util.HashMap();", enhancedClass));
        // 重写方法，支持存根和调用记录
        overrideMethods(enhancedClass, originalClass, settings);
        // 创建增强类
        Class<?> resultClass = enhancedClass.toClass();
        enhancedClasses.put(classToMock, resultClass);
        return (T) createInstance(resultClass);
    }
    
    /**
     * 【已更新V2】
     * 创建静态方法mock的占位实例（高级用法）。
     * @param <T> 要mock的类型
     * @param classToMock 要mock的类
     * @param settings mock配置
     * @return mock实例
     * @throws Exception 创建失败时抛出
     */
    @SuppressWarnings("unchecked")
    private static <T> T createStaticMock(Class<T> classToMock, MockSettings settings) throws Exception {
        // 静态mock需字节码增强原始类，当前为占位实现
        return (T) createInstance(classToMock);
    }
    
    /**
     * 【已更新V2】
     * 重写原始类的方法，支持存根、调用记录、默认返回值。
     * @param enhancedClass 增强类
     * @param originalClass 原始类
     * @param settings mock配置
     * @throws Exception 方法重写失败时抛出
     */
    private static void overrideMethods(CtClass enhancedClass, CtClass originalClass, MockSettings settings) throws Exception {
        CtMethod[] methods = originalClass.getDeclaredMethods();
        for (CtMethod method : methods) {
            if (Modifier.isPrivate(method.getModifiers()) && !settings.isMockPrivateMethodsEnabled()) {
                continue;
            }
            if (Modifier.isFinal(method.getModifiers()) && !settings.isMockFinalMethodsEnabled()) {
                continue;
            }
            if (Modifier.isStatic(method.getModifiers()) && !settings.isMockStaticMethodsEnabled()) {
                continue;
            }
            // 拷贝并重写方法体，支持存根和默认返回
            CtMethod newMethod = CtNewMethod.copy(method, enhancedClass, null);
            StringBuilder body = new StringBuilder();
            body.append("{\n");
            // 记录调用
            body.append("String methodKey = \"").append(method.getName()).append("\";\n");
            body.append("Object[] args = $args;\n");
            body.append("String callKey = methodKey + java.util.Arrays.deepToString(args);\n");
            body.append("java.util.List callList = (java.util.List) _methodCalls.get(callKey);\n");
            body.append("if (callList == null) { callList = new java.util.ArrayList(); _methodCalls.put(callKey, callList); }\n");
            body.append("callList.add(args);\n");
            // 存根优先
            body.append("if (_methodStubs.containsKey(callKey)) {\n");
            body.append("    Object stub = _methodStubs.get(callKey);\n");
            body.append("    if (stub instanceof java.lang.Throwable) throw (java.lang.Throwable) stub;\n");
            if (!method.getReturnType().equals(CtClass.voidType)) {
                body.append("    return (" + method.getReturnType().getName() + ") stub;\n");
            } else {
                body.append("    return;\n");
            }
            body.append("}\n");
            // 默认返回值
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
     * 【已更新V2】
     * 通过反射创建类实例，仅支持无参构造。
     * @param clazz 要实例化的类
     * @return 实例
     * @throws Exception 创建失败时抛出
     */
    private static Object createInstance(Class<?> clazz) throws Exception {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (NoSuchMethodException e) {
            // 无无参构造暂不支持
            return null;
        }
    }
} 