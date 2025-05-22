package com.mocktutorial.basics.mocks;

import com.mocktutorial.basics.controllers.UserController;
import com.mocktutorial.basics.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class demonstrating manual mocking
 */
class ManualMockUserServiceTest {

    private ManualMockUserService mockService;
    private UserController userController;

    @BeforeEach
    void setUp() {
        System.out.println("[ManualMock] 初始化mockService和userController");
        mockService = new ManualMockUserService();
        mockService.addMockUser(new User(1L, "John Doe", "john@example.com"));
        mockService.addMockUser(new User(2L, "Jane Smith", "jane@example.com"));
        userController = new UserController(mockService);
    }

    @Test
    void testFindById() {
        System.out.println("[ManualMock] 测试findById(1L)预期有值");
        Optional<User> user = mockService.findById(1L);
        System.out.println("实际: " + user);
        assertTrue(user.isPresent(), "findById(1L)应有值");
        assertEquals("John Doe", user.get().getName(), "findById(1L)应为John Doe");
        System.out.println("[通过] findById(1L)返回John Doe");
        System.out.println("[ManualMock] 测试findById(999L)预期无值");
        Optional<User> nonExistentUser = mockService.findById(999L);
        System.out.println("实际: " + nonExistentUser);
        assertFalse(nonExistentUser.isPresent(), "findById(999L)应无值");
        System.out.println("[通过] findById(999L)无值");
    }

    @Test
    void testSaveUser() {
        System.out.println("[ManualMock] 测试saveUser");
        User newUser = new User(3L, "Bob Johnson", "bob@example.com");
        boolean result = mockService.saveUser(newUser);
        System.out.println("saveUser实际: " + result);
        assertTrue(result, "saveUser应返回true");
        assertTrue(mockService.verifySavedUser(newUser), "verifySavedUser应为true");
        System.out.println("[通过] saveUser和verifySavedUser");
        Optional<User> savedUser = mockService.findById(3L);
        System.out.println("findById(3L)实际: " + savedUser);
        assertTrue(savedUser.isPresent(), "findById(3L)应有值");
        assertEquals("Bob Johnson", savedUser.get().getName(), "findById(3L)应为Bob Johnson");
        System.out.println("[通过] findById(3L)返回Bob Johnson");
    }

    @Test
    void testDeleteUser() {
        System.out.println("[ManualMock] 测试deleteUser(1L)");
        boolean result = mockService.deleteUser(1L);
        System.out.println("deleteUser(1L)实际: " + result);
        assertTrue(result, "deleteUser(1L)应返回true");
        assertTrue(mockService.verifyDeletedUser(1L), "verifyDeletedUser应为true");
        System.out.println("[通过] deleteUser(1L)和verifyDeletedUser");
        Optional<User> deletedUser = mockService.findById(1L);
        System.out.println("findById(1L)实际: " + deletedUser);
        assertFalse(deletedUser.isPresent(), "findById(1L)应无值");
        System.out.println("[通过] findById(1L)无值");
        System.out.println("[ManualMock] 测试deleteUser(999L)");
        boolean failedResult = mockService.deleteUser(999L);
        System.out.println("deleteUser(999L)实际: " + failedResult);
        assertFalse(failedResult, "deleteUser(999L)应返回false");
        System.out.println("[通过] deleteUser(999L)返回false");
    }

    @Test
    void testFindAllUsers() {
        System.out.println("[ManualMock] 测试findAllUsers");
        List<User> allUsers = mockService.findAllUsers();
        System.out.println("findAllUsers实际: " + allUsers);
        assertEquals(2, allUsers.size(), "findAllUsers应返回2个用户");
        assertTrue(allUsers.stream().anyMatch(user -> user.getId() == 1L), "应包含id=1L");
        assertTrue(allUsers.stream().anyMatch(user -> user.getId() == 2L), "应包含id=2L");
        System.out.println("[通过] findAllUsers内容正确");
    }
    
    @Test
    void testUserController() {
        System.out.println("[ManualMock] 测试userController.getUserName(1L)");
        String userName = userController.getUserName(1L);
        System.out.println("实际: " + userName);
        assertEquals("John Doe", userName, "getUserName(1L)应为John Doe");
        System.out.println("[通过] getUserName(1L)返回John Doe");
        System.out.println("[ManualMock] 测试userController.getUserName(999L)");
        String unknownUserName = userController.getUserName(999L);
        System.out.println("实际: " + unknownUserName);
        assertEquals("Unknown User", unknownUserName, "getUserName(999L)应为Unknown User");
        System.out.println("[通过] getUserName(999L)返回Unknown User");
        System.out.println("[ManualMock] 测试userController.createUser");
        boolean createResult = userController.createUser(3L, "Bob Johnson", "bob@example.com");
        System.out.println("createUser实际: " + createResult);
        assertTrue(createResult, "createUser应返回true");
        User expectedUser = new User(3L, "Bob Johnson", "bob@example.com");
        assertTrue(mockService.verifySavedUser(expectedUser), "verifySavedUser应为true");
        System.out.println("[通过] createUser和verifySavedUser");
        System.out.println("[ManualMock] 测试userController.updateUser");
        boolean updateResult = userController.updateUser(1L, "John Updated", "john.updated@example.com");
        System.out.println("updateUser实际: " + updateResult);
        assertTrue(updateResult, "updateUser应返回true");
        Optional<User> updatedUser = mockService.findById(1L);
        System.out.println("findById(1L) after update实际: " + updatedUser);
        assertTrue(updatedUser.isPresent(), "update后findById(1L)应有值");
        assertEquals("John Updated", updatedUser.get().getName(), "update后name应为John Updated");
        assertEquals("john.updated@example.com", updatedUser.get().getEmail(), "update后email应为john.updated@example.com");
        System.out.println("[通过] updateUser和findById(1L)内容正确");
    }
} 