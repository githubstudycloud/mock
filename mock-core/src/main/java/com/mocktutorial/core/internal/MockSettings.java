package com.mocktutorial.core.internal;

/**
 * Settings for configuring mock objects.
 */
public class MockSettings {
    private boolean enhancedMockEnabled = false;
    private boolean mockPrivateMethods = false;
    private boolean mockStaticMethods = false;
    private boolean mockFinalMethods = false;
    private boolean mockConstructors = false;
    private String name = null;
    private ClassLoader classLoader = null;
    
    /**
     * Enables enhanced mocking capabilities.
     * 
     * @return this settings instance for chaining
     */
    public MockSettings useEnhancedMock() {
        this.enhancedMockEnabled = true;
        return this;
    }
    
    /**
     * Enables mocking of private methods.
     * 
     * @return this settings instance for chaining
     */
    public MockSettings mockPrivateMethods() {
        this.mockPrivateMethods = true;
        return this;
    }
    
    /**
     * Enables mocking of static methods.
     * 
     * @return this settings instance for chaining
     */
    public MockSettings mockStaticMethods() {
        this.mockStaticMethods = true;
        return this;
    }
    
    /**
     * Enables mocking of final methods.
     * 
     * @return this settings instance for chaining
     */
    public MockSettings mockFinalMethods() {
        this.mockFinalMethods = true;
        return this;
    }
    
    /**
     * Enables mocking of constructors.
     * 
     * @return this settings instance for chaining
     */
    public MockSettings mockConstructors() {
        this.mockConstructors = true;
        return this;
    }
    
    /**
     * Sets a name for this mock.
     * 
     * @param name the name for the mock
     * @return this settings instance for chaining
     */
    public MockSettings name(String name) {
        this.name = name;
        return this;
    }
    
    /**
     * Sets a custom class loader for this mock.
     * 
     * @param classLoader the class loader to use
     * @return this settings instance for chaining
     */
    public MockSettings classLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }
    
    /**
     * Checks if enhanced mocking is enabled.
     * 
     * @return true if enhanced mocking is enabled
     */
    public boolean isEnhancedMockEnabled() {
        return enhancedMockEnabled;
    }
    
    /**
     * Checks if mocking of private methods is enabled.
     * 
     * @return true if mocking private methods is enabled
     */
    public boolean isMockPrivateMethodsEnabled() {
        return mockPrivateMethods;
    }
    
    /**
     * Checks if mocking of static methods is enabled.
     * 
     * @return true if mocking static methods is enabled
     */
    public boolean isMockStaticMethodsEnabled() {
        return mockStaticMethods;
    }
    
    /**
     * Checks if mocking of final methods is enabled.
     * 
     * @return true if mocking final methods is enabled
     */
    public boolean isMockFinalMethodsEnabled() {
        return mockFinalMethods;
    }
    
    /**
     * Checks if mocking of constructors is enabled.
     * 
     * @return true if mocking constructors is enabled
     */
    public boolean isMockConstructorsEnabled() {
        return mockConstructors;
    }
    
    /**
     * Gets the name for this mock.
     * 
     * @return the mock name, or null if not set
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the class loader for this mock.
     * 
     * @return the class loader, or null if not set
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }
} 