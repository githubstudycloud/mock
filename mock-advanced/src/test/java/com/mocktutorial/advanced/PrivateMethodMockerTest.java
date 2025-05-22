package com.mocktutorial.advanced;

import com.mocktutorial.basics.models.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class demonstrating private method mocking.
 */
class PrivateMethodMockerTest {
    
    private User user;
    
    @BeforeEach
    void setUp() {
        System.out.println("[PrivateMock] 初始化User对象");
        user = new User(1L, "John Doe", "john@example.com");
    }
    
    @AfterEach
    void tearDown() {
        PrivateMethodMocker.resetAll();
    }
    
    @Test
    void testInvokePrivateMethod() throws Exception {
        System.out.println("[PrivateMock] 直接反射调用private calculateScore");
        int score = PrivateMethodMocker.invokePrivateMethod(user, "calculateScore", new Class<?>[0]);
        System.out.println("实际: " + score);
        assertEquals(18, score, "calculateScore应返回18");
        System.out.println("[通过] 反射调用private方法正确");
    }
    
    @Test
    void testMockPrivateMethod() throws Throwable {
        System.out.println("[PrivateMock] 配置calculateScore返回100");
        PrivateMethodMocker.when(user, "calculateScore", 100);
        System.out.println("[PrivateMock] handler调用calculateScore，预期返回100");
        Object mockConfig = PrivateMethodMocker.handlePrivateMethodCall(user, "calculateScore", new Object[0]);
        System.out.println("实际: " + mockConfig);
        assertEquals(100, mockConfig, "mock handler应返回100");
        System.out.println("[通过] private方法mock配置生效");
    }
    
    @Test
    void testMockPrivateMethodWithException() {
        System.out.println("[PrivateMock] 配置calculateScore抛出异常");
        RuntimeException expectedException = new RuntimeException("Test exception");
        PrivateMethodMocker.whenThrow(user, "calculateScore", expectedException);
        System.out.println("[PrivateMock] handler调用calculateScore，预期抛出异常");
        try {
            Object configuredBehavior = PrivateMethodMocker.getConfiguredBehavior(user, "calculateScore");
            System.out.println("实际: " + configuredBehavior);
            assertNotNull(configuredBehavior, "应有mock配置");
            assertTrue(configuredBehavior instanceof RuntimeException, "配置应为异常");
            assertSame(expectedException, configuredBehavior, "应为配置的异常");
            System.out.println("[通过] private方法异常mock配置生效");
        } catch (Exception e) {
            System.out.println("[失败] private方法异常mock配置未生效: " + e.getMessage());
            throw e;
        }
    }
    
    @Test
    void testMockPrivateMethodWithCustomImplementation() throws Throwable {
        System.out.println("[PrivateMock] 配置calculateScore自定义实现");
        PrivateMethodMocker.whenImplement(user, "calculateScore", args -> 200);
        System.out.println("[PrivateMock] handler调用calculateScore，预期返回200");
        Object result = PrivateMethodMocker.handlePrivateMethodCall(user, "calculateScore", new Object[0]);
        System.out.println("实际: " + result);
        assertEquals(200, result, "自定义实现应返回200");
        System.out.println("[通过] private方法自定义实现mock配置生效");
    }
} 