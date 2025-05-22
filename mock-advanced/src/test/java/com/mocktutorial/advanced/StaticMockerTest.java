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
        System.out.println("[StaticMock] 准备静态方法mock");
        StaticMocker.prepareForStaticMocking(StaticTestClass.class);
        System.out.println("[StaticMock] 配置staticMethod返回'模拟返回值'");
        StaticMocker.when(StaticTestClass.class, "staticMethod", "模拟返回值");
        System.out.println("[StaticMock] 调用实际静态方法，预期返回'实际静态方法'");
        String originalResult = StaticTestClass.staticMethod();
        System.out.println("实际: " + originalResult);
        assertEquals("实际静态方法", originalResult, "静态方法未被mock时应返回原始值");
        System.out.println("[StaticMock] 直接调用mock handler，预期返回'模拟返回值'");
        Object mockResult = StaticMocker.handleStaticMethodCall(StaticTestClass.class, "staticMethod", new Object[0]);
        System.out.println("实际: " + mockResult);
        assertEquals("模拟返回值", mockResult, "mock handler应返回模拟值");
        System.out.println("[通过] 静态方法mock配置生效");
    }
    
    @Test
    public void testStaticMethodExceptionMocking() throws Throwable {
        System.out.println("[StaticMock] 配置staticMethod抛出异常");
        StaticMocker.prepareForStaticMocking(StaticTestClass.class);
        RuntimeException testException = new RuntimeException("测试异常");
        StaticMocker.whenThrow(StaticTestClass.class, "staticMethod", testException);
        System.out.println("[StaticMock] 调用mock handler，预期抛出异常");
        try {
            StaticMocker.handleStaticMethodCall(StaticTestClass.class, "staticMethod", new Object[0]);
            fail("应该抛出异常");
        } catch (Exception e) {
            System.out.println("实际异常: " + e);
            assertSame(testException, e, "抛出的异常应为配置的异常");
            System.out.println("[通过] 静态方法异常mock配置生效");
        }
    }
    
    @Test
    public void testStaticMethodCustomImplementation() throws Throwable {
        System.out.println("[StaticMock] 配置staticMethodWithParams自定义实现");
        StaticMocker.prepareForStaticMocking(StaticTestClass.class);
        StaticMocker.whenImplement(StaticTestClass.class, "staticMethodWithParams", args -> "自定义实现：" + args[0]);
        System.out.println("[StaticMock] 调用mock handler，预期返回自定义实现结果");
        Object result = StaticMocker.handleStaticMethodCall(StaticTestClass.class, "staticMethodWithParams", new Object[]{"test"});
        System.out.println("实际: " + result);
        assertEquals("自定义实现：test", result, "自定义实现应生效");
        System.out.println("[通过] 静态方法自定义实现mock配置生效");
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