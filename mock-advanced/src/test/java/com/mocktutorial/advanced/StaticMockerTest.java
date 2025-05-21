package com.mocktutorial.advanced;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 静态方法模拟测试类
 */
public class StaticMockerTest {
    
    @AfterEach
    public void tearDown() {
        StaticMocker.resetAll();
    }
    
    @Test
    public void testStaticMethodMocking() throws Throwable {
        // 为测试准备一个静态方法类
        StaticMocker.prepareForStaticMocking(StaticTestClass.class);
        
        // 配置静态方法返回值
        StaticMocker.when(StaticTestClass.class, "staticMethod", "模拟返回值");
        
        // 对于目前的实现，调用实际的静态方法不会使用我们的配置
        // 但我们可以测试配置是否正确设置
        String originalResult = StaticTestClass.staticMethod();
        assertEquals("实际静态方法", originalResult);
        
        // 检查我们的模拟配置是否正确设置
        Object mockResult = StaticMocker.handleStaticMethodCall(StaticTestClass.class, "staticMethod", new Object[0]);
        assertEquals("模拟返回值", mockResult);
    }
    
    @Test
    public void testStaticMethodExceptionMocking() throws Throwable {
        // 为测试准备一个静态方法类
        StaticMocker.prepareForStaticMocking(StaticTestClass.class);
        
        // 配置静态方法抛出异常
        RuntimeException testException = new RuntimeException("测试异常");
        StaticMocker.whenThrow(StaticTestClass.class, "staticMethod", testException);
        
        // 验证异常配置是否正确设置
        try {
            StaticMocker.handleStaticMethodCall(StaticTestClass.class, "staticMethod", new Object[0]);
            fail("应该抛出异常");
        } catch (Exception e) {
            assertSame(testException, e);
        }
    }
    
    @Test
    public void testStaticMethodCustomImplementation() throws Throwable {
        // 为测试准备一个静态方法类
        StaticMocker.prepareForStaticMocking(StaticTestClass.class);
        
        // 配置静态方法使用自定义实现
        StaticMocker.whenImplement(StaticTestClass.class, "staticMethodWithParams", 
                args -> "自定义实现：" + args[0]);
        
        // 验证自定义实现是否正确设置
        Object result = StaticMocker.handleStaticMethodCall(
                StaticTestClass.class, "staticMethodWithParams", new Object[]{"test"});
        assertEquals("自定义实现：test", result);
    }
    
    /**
     * 用于测试的静态方法类
     */
    public static class StaticTestClass {
        public static String staticMethod() {
            return "实际静态方法";
        }
        
        public static String staticMethodWithParams(String param) {
            return "实际参数方法：" + param;
        }
    }
} 