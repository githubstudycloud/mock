package com.mocktutorial.basics.models;

/**
 * A simple User model for demonstration purposes
 */
public class User {
    private final long id;
    private final String name;
    private final String email;
    
    public User(long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
    
    public User(long id, String name) {
        this(id, name, null);
    }
    
    public long getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getEmail() {
        return email;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        User user = (User) o;
        
        if (id != user.id) return false;
        if (name != null ? !name.equals(user.name) : user.name != null) return false;
        return email != null ? email.equals(user.email) : user.email == null;
    }
    
    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        return result;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
    
    /**
     * A private method to demonstrate mocking private methods
     * @return calculated score based on user data
     */
    private int calculateScore() {
        // Simple algorithm for demonstration
        return (int) (id * 10 + (name != null ? name.length() : 0));
    }
    
    /**
     * Gets user score
     * @return user score
     */
    public int getScore() {
        return calculateScore();
    }
} 