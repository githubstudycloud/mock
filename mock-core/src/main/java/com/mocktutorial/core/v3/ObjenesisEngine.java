package com.mocktutorial.core.v3;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * V3 ObjenesisEngine: 无Agent场景下对象分配和mock实现骨架。
 */
public class ObjenesisEngine {
    private final Objenesis objenesis = new ObjenesisStd();

    /**
     * 创建一个未调用构造函数的实例。
     */
    public <T> T createInstance(Class<T> clazz) {
        if (clazz.isInterface()) {
            // JDK Proxy for interfaces
            Object proxy = Proxy.newProxyInstance(
                    clazz.getClassLoader(),
                    new Class[]{clazz},
                    new MockInvocationHandler()
            );
            return clazz.cast(proxy);
        } else {
            // 普通类用cglib生成代理
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(clazz);
            enhancer.setCallback(new CglibMockInterceptor());
            @SuppressWarnings("unchecked")
            T proxy = (T) enhancer.create();
            return proxy;
        }
    }

    /**
     * JDK Proxy方法拦截器：查找MockFactory行为并执行
     */
    private static class MockInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            // 处理基础方法，避免递归
            if (name.equals("hashCode") && method.getParameterCount() == 0) {
                return System.identityHashCode(proxy);
            }
            if (name.equals("equals") && method.getParameterCount() == 1) {
                return proxy == args[0];
            }
            if (name.equals("toString") && method.getParameterCount() == 0) {
                return "MockProxy(" + proxy.getClass().getInterfaces()[0].getSimpleName() + ")@" + Integer.toHexString(System.identityHashCode(proxy));
            }
            MockFactory.recordInvocation(proxy, method.getName(), args);
            MockFactory.MockBehavior behavior = MockFactory.findBehavior(proxy, method.getName(), args);
            if (behavior != null) {
                if (behavior.throwable != null) throw behavior.throwable;
                return behavior.returnValue;
            }
            // 默认返回null或基本类型默认值
            Class<?> returnType = method.getReturnType();
            if (returnType.isPrimitive()) {
                if (returnType == boolean.class) return false;
                if (returnType == char.class) return '\0';
                if (returnType == byte.class) return (byte)0;
                if (returnType == short.class) return (short)0;
                if (returnType == int.class) return 0;
                if (returnType == long.class) return 0L;
                if (returnType == float.class) return 0f;
                if (returnType == double.class) return 0d;
            }
            return null;
        }
    }

    /**
     * cglib方法拦截器：查找MockFactory行为并执行
     */
    private static class CglibMockInterceptor implements MethodInterceptor {
        @Override
        public Object intercept(Object obj, java.lang.reflect.Method method, Object[] args, MethodProxy proxy) throws Throwable {
            String name = method.getName();
            // 处理基础方法，避免递归
            if (name.equals("hashCode") && method.getParameterCount() == 0) {
                return System.identityHashCode(obj);
            }
            if (name.equals("equals") && method.getParameterCount() == 1) {
                return obj == args[0];
            }
            if (name.equals("toString") && method.getParameterCount() == 0) {
                return "MockProxy(" + obj.getClass().getSuperclass().getSimpleName() + ")@" + Integer.toHexString(System.identityHashCode(obj));
            }
            MockFactory.recordInvocation(obj, method.getName(), args);
            MockFactory.MockBehavior behavior = MockFactory.findBehavior(obj, method.getName(), args);
            if (behavior != null) {
                if (behavior.throwable != null) throw behavior.throwable;
                return behavior.returnValue;
            }
            // 默认返回null或基本类型默认值
            Class<?> returnType = method.getReturnType();
            if (returnType.isPrimitive()) {
                if (returnType == boolean.class) return false;
                if (returnType == char.class) return '\0';
                if (returnType == byte.class) return (byte)0;
                if (returnType == short.class) return (short)0;
                if (returnType == int.class) return 0;
                if (returnType == long.class) return 0L;
                if (returnType == float.class) return 0f;
                if (returnType == double.class) return 0d;
            }
            return null;
        }
    }

    // TODO: 方法拦截、代理等mock实现骨架
} 