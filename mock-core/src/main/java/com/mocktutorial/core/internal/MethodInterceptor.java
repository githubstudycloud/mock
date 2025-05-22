package com.mocktutorial.core.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import com.mocktutorial.core.Mock;

/**
 * 【已更新V2】
 * mock方法行为存根和自定义实现的链式配置器。
 * <ul>
 *   <li>支持thenReturn/thenThrow/thenImplement链式API。</li>
 *   <li>所有存根均为本mock实例独立，无全局状态。</li>
 *   <li>支持接口和类的mock，底层自动区分JDK Proxy和字节码增强。</li>
 * </ul>
 * <p>
 * 典型用法：
 * <pre>
 *   Mock.when(mock, "findById", 1L).thenReturn(Optional.of(user));
 *   Mock.when(mock, "findById", 2L).thenThrow(new RuntimeException());
 *   Mock.when(mock, "findById", 3L).thenImplement(args -> ...);
 * </pre>
 * @param <T> mock类型
 */
public class MethodInterceptor<T> {
    private final T mock;
    private final String methodName;
    private final Object[] args;
    
    /**
     * 【已更新V2】
     * 创建方法拦截器，内部用于注册存根。
     * @param mock mock对象
     * @param methodName 方法名
     * @param args 方法参数
     */
    public MethodInterceptor(T mock, String methodName, Object[] args) {
        this.mock = mock;
        this.methodName = methodName;
        this.args = args == null ? new Object[0] : args.clone();
    }
    
    /**
     * 【已更新V2】
     * 配置方法返回指定值。
     * @param <R> 返回类型
     * @param returnValue 返回值
     * @return ResultBuilder，可继续链式配置
     */
    @SuppressWarnings("unchecked")
    public <R> ResultBuilder<R> thenReturn(R returnValue) {
        if (mock != null) {
            if (java.lang.reflect.Proxy.isProxyClass(mock.getClass())) {
                com.mocktutorial.core.internal.MockitoAdapter.MockInvocationHandler.stubReturn(mock, methodName, args, returnValue);
            } else {
                try {
                    java.lang.reflect.Field stubsField = mock.getClass().getDeclaredField("_methodStubs");
                    stubsField.setAccessible(true);
                    java.util.Map stubs = (java.util.Map) stubsField.get(mock);
                    String callKey = methodName + java.util.Arrays.deepToString(args);
                    stubs.put(callKey, returnValue);
                } catch (Exception e) { throw new RuntimeException(e); }
            }
        }
        return new ResultBuilder<>(null);
    }
    
    /**
     * 【已更新V2】
     * 配置方法抛出指定异常。
     * @param throwable 要抛出的异常
     * @return ResultBuilder，可继续链式配置
     */
    @SuppressWarnings("unchecked")
    public <R> ResultBuilder<R> thenThrow(Throwable throwable) {
        if (mock != null) {
            if (java.lang.reflect.Proxy.isProxyClass(mock.getClass())) {
                com.mocktutorial.core.internal.MockitoAdapter.MockInvocationHandler.stubThrow(mock, methodName, args, throwable);
            } else {
                try {
                    java.lang.reflect.Field stubsField = mock.getClass().getDeclaredField("_methodStubs");
                    stubsField.setAccessible(true);
                    java.util.Map stubs = (java.util.Map) stubsField.get(mock);
                    String callKey = methodName + java.util.Arrays.deepToString(args);
                    stubs.put(callKey, throwable);
                } catch (Exception e) { throw new RuntimeException(e); }
            }
        }
        return new ResultBuilder<>(null);
    }
    
    /**
     * 【已更新V2】
     * 配置方法自定义实现。
     * @param <R> 返回类型
     * @param implementation 实现函数，参数为方法参数数组
     * @return ResultBuilder，可继续链式配置
     */
    @SuppressWarnings("unchecked")
    public <R> ResultBuilder<R> thenImplement(Function<Object[], R> implementation) {
        if (mock != null) {
            if (java.lang.reflect.Proxy.isProxyClass(mock.getClass())) {
                com.mocktutorial.core.internal.MockitoAdapter.MockInvocationHandler.stubImpl(mock, methodName, args, (Function<Object[], Object>) implementation);
            } else {
                try {
                    java.lang.reflect.Field stubsField = mock.getClass().getDeclaredField("_methodStubs");
                    stubsField.setAccessible(true);
                    java.util.Map stubs = (java.util.Map) stubsField.get(mock);
                    String callKey = methodName + java.util.Arrays.deepToString(args);
                    stubs.put(callKey, implementation);
                } catch (Exception e) { throw new RuntimeException(e); }
            }
        }
        return new ResultBuilder<>(null);
    }
    
    /**
     * 【已更新V2】
     * 存根结果链式配置器。
     * @param <R> 返回类型
     */
    public static class ResultBuilder<R> {
        private final R methodReturnValue;
        
        ResultBuilder(R methodReturnValue) {
            this.methodReturnValue = methodReturnValue;
        }
        
        /**
         * 获取原始方法返回值（一般用于链式扩展）。
         * @return 方法返回值
         */
        public R getMethodReturnValue() {
            return methodReturnValue;
        }
    }
} 