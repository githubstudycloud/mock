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
        // 测试创建接口的mock
        UserService userService = Mock.mock(UserService.class);
        
        // 默认情况下，接口方法应该返回null或基本类型的默认值
        assertNull(userService.findById(1L).orElse(null));
        assertFalse(userService.saveUser(new User(1L, "Test")));
        assertFalse(userService.deleteUser(1L));
        assertNotNull(userService.findAllUsers());
        assertTrue(userService.findAllUsers().isEmpty());
    }
    
    @Test
    public void testMockClass() {
        // 测试创建具体类的mock
        User mockUser = Mock.mock(User.class);
        
        // 默认情况下，方法应该返回基本类型的默认值或null
        assertEquals(0L, mockUser.getId());
        assertNull(mockUser.getName());
        assertNull(mockUser.getEmail());
        assertEquals(0, mockUser.getScore());
    }
    
    @Test
    public void testToString() {
        // 测试mock对象的toString方法
        UserService userService = Mock.mock(UserService.class);
        
        // toString应该包含类名和某种标识
        String str = userService.toString();
        assertTrue(str.contains("UserService"));
        assertTrue(str.contains("Mock"));
    }
    
    @Test
    public void testEquals() {
        // 测试mock对象的equals方法
        UserService userService1 = Mock.mock(UserService.class);
        UserService userService2 = Mock.mock(UserService.class);
        
        // 相同的mock对象应该相等于自身
        assertEquals(userService1, userService1);
        assertEquals(userService2, userService2);
        
        // 不同的mock对象应该不相等
        assertNotEquals(userService1, userService2);
        assertNotEquals(userService1, null);
        assertNotEquals(userService1, "string");
    }
} 