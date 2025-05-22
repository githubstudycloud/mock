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
        // 1. 创建mock对象
        UserService mock = Mock.mock(UserService.class);
        // 2. 配置存根
        Mock.when(mock, "findById", 1L).thenReturn(Optional.of(new User(1L, "Restored")));
        // 3. 验证存根生效
        Optional<User> userOpt = mock.findById(1L);
        assertTrue(userOpt.isPresent());
        assertEquals("Restored", userOpt.get().getName());
        // 4. 还原mock
        Mock.reset(mock);
        // 5. 验证还原后为默认值（Optional.empty）
        Optional<User> userOpt2 = mock.findById(1L);
        assertNotNull(userOpt2);
        assertFalse(userOpt2.isPresent());
    }
} 