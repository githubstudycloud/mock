package com.mocktutorial.basics.mocks;

import com.mocktutorial.basics.models.User;
import com.mocktutorial.basics.services.UserService;

import java.util.*;

/**
 * A manual mock implementation of UserService for demonstration purposes
 */
public class ManualMockUserService implements UserService {
    private final Map<Long, User> mockUsers = new HashMap<>();
    private final List<User> savedUsers = new ArrayList<>();
    private final Set<Long> deletedUserIds = new HashSet<>();
    
    /**
     * Add a mock user for testing
     * @param user the user to add to the mock database
     */
    public void addMockUser(User user) {
        mockUsers.put(user.getId(), user);
    }
    
    @Override
    public Optional<User> findById(long id) {
        return Optional.ofNullable(mockUsers.get(id));
    }
    
    @Override
    public boolean saveUser(User user) {
        savedUsers.add(user);
        mockUsers.put(user.getId(), user);
        return true;
    }
    
    @Override
    public boolean deleteUser(long id) {
        if (mockUsers.containsKey(id)) {
            mockUsers.remove(id);
            deletedUserIds.add(id);
            return true;
        }
        return false;
    }
    
    @Override
    public List<User> findAllUsers() {
        return new ArrayList<>(mockUsers.values());
    }
    
    /**
     * Verify if a user was saved
     * @param user the user to verify
     * @return true if the user was saved, false otherwise
     */
    public boolean verifySavedUser(User user) {
        return savedUsers.contains(user);
    }
    
    /**
     * Verify if a user was deleted
     * @param id the ID of the user to verify
     * @return true if the user was deleted, false otherwise
     */
    public boolean verifyDeletedUser(long id) {
        return deletedUserIds.contains(id);
    }
    
    /**
     * Get the number of times a user was saved
     * @param user the user to check
     * @return the count of save operations for this user
     */
    public int getSaveCount(User user) {
        int count = 0;
        for (User saved : savedUsers) {
            if (saved.equals(user)) {
                count++;
            }
        }
        return count;
    }
} 