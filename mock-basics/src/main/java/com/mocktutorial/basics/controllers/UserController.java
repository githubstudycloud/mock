package com.mocktutorial.basics.controllers;

import com.mocktutorial.basics.models.User;
import com.mocktutorial.basics.services.UserService;

import java.util.List;
import java.util.Optional;

/**
 * Controller for managing users
 */
public class UserController {
    private final UserService userService;
    
    /**
     * Create a new UserController with the specified UserService
     * @param userService the service to use for user operations
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * Get a user's name by their ID
     * @param userId the ID of the user
     * @return the user's name, or "Unknown User" if not found
     */
    public String getUserName(long userId) {
        Optional<User> user = userService.findById(userId);
        return user.map(User::getName).orElse("Unknown User");
    }
    
    /**
     * Create a new user
     * @param id the user ID
     * @param name the user name
     * @param email the user email
     * @return true if created successfully, false otherwise
     */
    public boolean createUser(long id, String name, String email) {
        return userService.saveUser(new User(id, name, email));
    }
    
    /**
     * Update a user's details
     * @param id the ID of the user to update
     * @param name the new name
     * @param email the new email
     * @return true if updated, false if user not found
     */
    public boolean updateUser(long id, String name, String email) {
        Optional<User> existingUser = userService.findById(id);
        if (existingUser.isPresent()) {
            User updatedUser = new User(id, name, email);
            return userService.saveUser(updatedUser);
        }
        return false;
    }
    
    /**
     * Delete a user
     * @param userId the ID of the user to delete
     * @return true if deleted, false if not found
     */
    public boolean deleteUser(long userId) {
        return userService.deleteUser(userId);
    }
    
    /**
     * Get all user names
     * @return a list of all user names
     */
    public List<String> getAllUserNames() {
        return userService.findAllUsers().stream()
                .map(User::getName)
                .toList();
    }
} 