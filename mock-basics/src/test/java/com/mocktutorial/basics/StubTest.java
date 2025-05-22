package com.mocktutorial.basics;

import com.mocktutorial.basics.models.User;
import com.mocktutorial.basics.services.UserService;
import com.mocktutorial.core.Mock;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 方法存根功能测试类
 * 注意：由于我们尚未完全实现字节码操作，这些测试主要展示API设计，而不是实际功能
 */
public class StubTest {

    @Test
    public void testMethodStubbing() {
        System.out.println("[Stub] 创建UserService mock对象");
        UserService userService = Mock.mock(UserService.class);
        User testUser = new User(1L, "Test User", "test@example.com");
        System.out.println("[Stub] 配置findById(1L)返回testUser");
        Mock.when(userService, "findById", 1L).thenReturn(Optional.of(testUser));
        System.out.println("[Stub] 调用findById(1L)，预期返回testUser");
        Optional<User> returnedUser = userService.findById(1L);
        System.out.println("实际: " + returnedUser);
        assertTrue(returnedUser.isPresent(), "findById应返回有值");
        assertEquals(testUser, returnedUser.get(), "findById返回的User应为testUser");
        System.out.println("[通过] findById存根生效");
    }
    
    @Test
    public void testExceptionStubbing() {
        System.out.println("[Stub] 创建UserService mock对象");
        UserService userService = Mock.mock(UserService.class);
        RuntimeException testException = new RuntimeException("测试异常");
        System.out.println("[Stub] 配置findById(999L)抛出异常");
        Mock.when(userService, "findById", 999L).thenThrow(testException);
        System.out.println("[Stub] 调用findById(999L)，预期抛出异常");
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> userService.findById(999L));
        System.out.println("实际异常信息: " + thrown.getMessage());
        assertEquals("测试异常", thrown.getMessage(), "异常信息应为'测试异常'");
        System.out.println("[通过] findById异常存根生效");
    }
    
    @Test
    public void testVerification() {
        System.out.println("[Verify] 创建UserService mock对象");
        UserService userService = Mock.mock(UserService.class);
        User testUser = new User(1L, "Test User", "test@example.com");
        System.out.println("[Verify] 执行findById和saveUser操作");
        userService.findById(1L);
        userService.saveUser(testUser);
        userService.saveUser(testUser); // 调用两次
        try {
            System.out.println("[Verify] 验证findById(1L)被调用1次");
            Mock.verify(userService).once().findById(1L);
            System.out.println("[通过] findById(1L)调用验证通过");
            System.out.println("[Verify] 验证saveUser(testUser)被调用2次");
            Mock.verify(userService).times(2).saveUser(testUser);
            System.out.println("[通过] saveUser(testUser)调用验证通过");
            System.out.println("[Verify] 验证deleteUser(1L)未被调用");
            Mock.verify(userService).never().deleteUser(1L);
            System.out.println("[通过] deleteUser(1L)未被调用验证通过");
        } catch (AssertionError e) {
            System.out.println("[警告] 方法验证未通过: " + e.getMessage());
            throw e;
        }
    }
} 