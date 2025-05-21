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
        // Create the mock service
        mockService = new ManualMockUserService();
        
        // Add test data
        mockService.addMockUser(new User(1L, "John Doe", "john@example.com"));
        mockService.addMockUser(new User(2L, "Jane Smith", "jane@example.com"));
        
        // Create controller with mock service
        userController = new UserController(mockService);
    }

    @Test
    void testFindById() {
        // Test finding an existing user
        Optional<User> user = mockService.findById(1L);
        assertTrue(user.isPresent());
        assertEquals("John Doe", user.get().getName());
        
        // Test finding a non-existent user
        Optional<User> nonExistentUser = mockService.findById(999L);
        assertFalse(nonExistentUser.isPresent());
    }

    @Test
    void testSaveUser() {
        // Create a new user
        User newUser = new User(3L, "Bob Johnson", "bob@example.com");
        
        // Save the user
        boolean result = mockService.saveUser(newUser);
        
        // Verify the save was successful
        assertTrue(result);
        assertTrue(mockService.verifySavedUser(newUser));
        
        // Verify the user can be retrieved
        Optional<User> savedUser = mockService.findById(3L);
        assertTrue(savedUser.isPresent());
        assertEquals("Bob Johnson", savedUser.get().getName());
    }

    @Test
    void testDeleteUser() {
        // Delete an existing user
        boolean result = mockService.deleteUser(1L);
        
        // Verify the delete was successful
        assertTrue(result);
        assertTrue(mockService.verifyDeletedUser(1L));
        
        // Verify the user can no longer be retrieved
        Optional<User> deletedUser = mockService.findById(1L);
        assertFalse(deletedUser.isPresent());
        
        // Test deleting a non-existent user
        boolean failedResult = mockService.deleteUser(999L);
        assertFalse(failedResult);
    }

    @Test
    void testFindAllUsers() {
        // Get all users
        List<User> allUsers = mockService.findAllUsers();
        
        // Verify the correct number of users is returned
        assertEquals(2, allUsers.size());
        
        // Verify specific users are in the list
        assertTrue(allUsers.stream().anyMatch(user -> user.getId() == 1L));
        assertTrue(allUsers.stream().anyMatch(user -> user.getId() == 2L));
    }
    
    @Test
    void testUserController() {
        // Test getting an existing user's name
        String userName = userController.getUserName(1L);
        assertEquals("John Doe", userName);
        
        // Test getting a non-existent user's name
        String unknownUserName = userController.getUserName(999L);
        assertEquals("Unknown User", unknownUserName);
        
        // Test creating a new user
        boolean createResult = userController.createUser(3L, "Bob Johnson", "bob@example.com");
        assertTrue(createResult);
        
        // Verify the user was saved
        User expectedUser = new User(3L, "Bob Johnson", "bob@example.com");
        assertTrue(mockService.verifySavedUser(expectedUser));
        
        // Test updating a user
        boolean updateResult = userController.updateUser(1L, "John Updated", "john.updated@example.com");
        assertTrue(updateResult);
        
        // Verify the user was updated
        Optional<User> updatedUser = mockService.findById(1L);
        assertTrue(updatedUser.isPresent());
        assertEquals("John Updated", updatedUser.get().getName());
        assertEquals("john.updated@example.com", updatedUser.get().getEmail());
    }
} 