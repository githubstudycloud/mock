package com.mocktutorial.basics;

import com.mocktutorial.basics.models.User;
import com.mocktutorial.basics.services.UserService;
import com.mocktutorial.core.Mock;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

public class MockRestoreTest {
    @Test
    public void testStubAndRestore() {
        System.out.println("[Restore] 创建UserService mock对象");
        UserService mock = Mock.mock(UserService.class);
        System.out.println("[Restore] 配置findById(1L)返回Restored");
        Mock.when(mock, "findById", 1L).thenReturn(Optional.of(new User(1L, "Restored")));
        System.out.println("[Restore] 调用findById(1L)，预期返回Restored");
        Optional<User> userOpt = mock.findById(1L);
        System.out.println("实际: " + userOpt);
        assertTrue(userOpt.isPresent(), "findById应返回有值");
        assertEquals("Restored", userOpt.get().getName(), "findById返回的User应为Restored");
        System.out.println("[通过] findById存根生效");
        System.out.println("[Restore] 调用Mock.reset还原mock");
        Mock.reset(mock);
        System.out.println("[Restore] 再次调用findById(1L)，预期返回Optional.empty");
        Optional<User> userOpt2 = mock.findById(1L);
        System.out.println("实际: " + userOpt2);
        assertNotNull(userOpt2, "findById应不为null");
        assertFalse(userOpt2.isPresent(), "findById应返回Optional.empty");
        System.out.println("[通过] reset后findById返回Optional.empty");
    }
} 