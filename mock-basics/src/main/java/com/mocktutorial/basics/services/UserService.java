package com.mocktutorial.basics.services;

import com.mocktutorial.basics.models.User;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for User operations
 */
public interface UserService {
    /**
     * Find a user by their ID
     * @param id the user ID
     * @return the user if found, otherwise Optional.empty()
     */
    Optional<User> findById(long id);
    
    /**
     * Save a user to the system
     * @param user the user to save
     * @return true if saved successfully, false otherwise
     */
    boolean saveUser(User user);
    
    /**
     * Delete a user by their ID
     * @param id the ID of the user to delete
     * @return true if deleted successfully, false if user not found
     */
    boolean deleteUser(long id);
    
    /**
     * Find all users in the system
     * @return list of all users
     */
    List<User> findAllUsers();
} 