package com.mocktutorial.core.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mocktutorial.core.Mock;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Adapter for using Mockito as a fallback when enhanced mocking isn't available or enabled.
 * In a real implementation, this would use actual Mockito methods, but to keep things simple,
 * we'll implement a basic JDK proxy-based mock here.
 */
public class MockitoAdapter {
    private static final Logger logger = LoggerFactory.getLogger(MockitoAdapter.class);
    
    /**
     * Creates a mock instance of the given class.
     * 
     * @param <T> the type to mock
     * @param classToMock the class to create a mock of
     * @return a mock instance
     */
    @SuppressWarnings("unchecked")
    public static <T> T createMock(Class<T> classToMock) {
        if (classToMock.isInterface()) {
            // For interfaces, use JDK dynamic proxies
            return (T) Proxy.newProxyInstance(
                    classToMock.getClassLoader(),
                    new Class<?>[] { classToMock },
                    new MockInvocationHandler(classToMock)
            );
        } else {
            // For classes, attempt to create a simple instance
            // In a real implementation, we would use a specialized library for subclassing
            try {
                Constructor<T> constructor = classToMock.getDeclaredConstructor();
                constructor.setAccessible(true);
                return constructor.newInstance();
            } catch (Exception e) {
                logger.error("Failed to create simple mock for class: " + classToMock.getName(), e);
                return null;
            }
        }
    }
    
    /**
     * Simple invocation handler for the JDK proxy.
     */
    private static class MockInvocationHandler implements InvocationHandler {
        private final Class<?> mockedInterface;
        private final Map<String, Object> methodReturns = new HashMap<>();
        
        MockInvocationHandler(Class<?> mockedInterface) {
            this.mockedInterface = mockedInterface;
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // Handle Object methods like equals, hashCode, and toString
            if (method.getName().equals("equals")) {
                return proxy == args[0];
            } else if (method.getName().equals("hashCode")) {
                return System.identityHashCode(proxy);
            } else if (method.getName().equals("toString")) {
                return "Mock of " + mockedInterface.getName() + "@" + Integer.toHexString(System.identityHashCode(proxy));
            }
            
            // 首先尝试执行方法并记录调用
            Object result = getDefaultReturnValue(method.getReturnType());
            
            // 记录方法调用，以便when()方法可以使用
            Mock.recordMethodCall(result);
            
            // 检查是否有为此方法配置的存根行为
            if (MethodInterceptor.hasReturnValue(result)) {
                // 如果有，使用配置的存根行为
                return MethodInterceptor.getReturnValue(result, args);
            }
            
            // 检查本地存根
            String methodKey = method.getName();
            if (methodReturns.containsKey(methodKey)) {
                Object returnValue = methodReturns.get(methodKey);
                if (returnValue instanceof Throwable) {
                    throw (Throwable) returnValue;
                }
                return returnValue;
            }
            
            return result;
        }
        
        /**
         * 获取方法返回类型的默认值
         *
         * @param returnType 返回类型
         * @return 默认值
         */
        private Object getDefaultReturnValue(Class<?> returnType) {
            if (returnType.equals(void.class)) {
                return null;
            } else if (returnType.equals(boolean.class) || returnType.equals(Boolean.class)) {
                return false;
            } else if (returnType.equals(char.class) || returnType.equals(Character.class)) {
                return (char) 0;
            } else if (returnType.isPrimitive() || Number.class.isAssignableFrom(returnType)) {
                if (returnType.equals(byte.class) || returnType.equals(Byte.class)) {
                    return (byte) 0;
                } else if (returnType.equals(short.class) || returnType.equals(Short.class)) {
                    return (short) 0;
                } else if (returnType.equals(int.class) || returnType.equals(Integer.class)) {
                    return 0;
                } else if (returnType.equals(long.class) || returnType.equals(Long.class)) {
                    return 0L;
                } else if (returnType.equals(float.class) || returnType.equals(Float.class)) {
                    return 0.0f;
                } else if (returnType.equals(double.class) || returnType.equals(Double.class)) {
                    return 0.0d;
                }
            }
            
            return null;
        }
    }
} 