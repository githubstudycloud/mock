package com.mocktutorial.basics;

import com.mocktutorial.basics.models.User;
import com.mocktutorial.basics.services.UserService;
import com.mocktutorial.core.Mock;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 基本Mock功能测试类
 */
public class MockTest {

    @Test
    public void testMockInterface() {
        System.out.println("[接口Mock] 创建UserService mock对象");
        UserService userService = Mock.mock(UserService.class);
        System.out.println("[接口Mock] 预期findById返回Optional.empty");
        Optional<User> result = userService.findById(1L);
        System.out.println("实际: " + result);
        assertNull(result.orElse(null), "findById应返回Optional.empty");
        System.out.println("[通过] findById返回Optional.empty");
        System.out.println("[接口Mock] 预期saveUser返回false");
        boolean saveResult = userService.saveUser(new User(1L, "Test"));
        System.out.println("实际: " + saveResult);
        assertFalse(saveResult, "saveUser应返回false");
        System.out.println("[通过] saveUser返回false");
        System.out.println("[接口Mock] 预期deleteUser返回false");
        boolean delResult = userService.deleteUser(1L);
        System.out.println("实际: " + delResult);
        assertFalse(delResult, "deleteUser应返回false");
        System.out.println("[通过] deleteUser返回false");
        System.out.println("[接口Mock] 预期findAllUsers返回空集合");
        assertNotNull(userService.findAllUsers(), "findAllUsers不应为null");
        assertTrue(userService.findAllUsers().isEmpty(), "findAllUsers应返回空集合");
        System.out.println("[通过] findAllUsers返回空集合");
    }
    
    @Test
    public void testMockClass() {
        System.out.println("[类Mock] 创建User mock对象");
        User mockUser = Mock.mock(User.class);
        System.out.println("[类Mock] 预期getId返回0");
        assertEquals(0L, mockUser.getId(), "getId应返回0");
        System.out.println("[通过] getId返回0");
        System.out.println("[类Mock] 预期getName返回null");
        assertNull(mockUser.getName(), "getName应返回null");
        System.out.println("[通过] getName返回null");
        System.out.println("[类Mock] 预期getEmail返回null");
        assertNull(mockUser.getEmail(), "getEmail应返回null");
        System.out.println("[通过] getEmail返回null");
        System.out.println("[类Mock] 预期getScore返回0");
        assertEquals(0, mockUser.getScore(), "getScore应返回0");
        System.out.println("[通过] getScore返回0");
    }
    
    @Test
    public void testToString() {
        System.out.println("[toString] 测试mock对象toString");
        UserService userService = Mock.mock(UserService.class);
        String str = userService.toString();
        System.out.println("实际: " + str);
        assertTrue(str.contains("UserService"), "toString应包含类名");
        assertTrue(str.contains("Mock"), "toString应包含Mock标识");
        System.out.println("[通过] toString包含类名和Mock标识");
    }
    
    @Test
    public void testEquals() {
        System.out.println("[equals] 测试mock对象equals");
        UserService userService1 = Mock.mock(UserService.class);
        UserService userService2 = Mock.mock(UserService.class);
        assertEquals(userService1, userService1, "同一mock对象应相等");
        assertEquals(userService2, userService2, "同一mock对象应相等");
        assertNotEquals(userService1, userService2, "不同mock对象不应相等");
        assertNotEquals(userService1, null, "与null不应相等");
        assertNotEquals(userService1, "string", "与其他类型不应相等");
        System.out.println("[通过] equals行为符合预期");
    }
} 