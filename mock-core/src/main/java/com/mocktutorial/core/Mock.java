package com.mocktutorial.core;

import com.mocktutorial.core.internal.MockitoAdapter;
import com.mocktutorial.core.internal.MethodInterceptor;
import com.mocktutorial.core.internal.MockCreator;
import com.mocktutorial.core.internal.MockSettings;

/**
 * Main entry point for the enhanced mock framework.
 * This class provides static methods to create and configure mocks.
 */
public class Mock {
    
    /**
     * Creates a mock instance of the given class.
     * 
     * @param <T> the type to mock
     * @param classToMock the class to create a mock of
     * @return a mock instance
     */
    public static <T> T mock(Class<T> classToMock) {
        return mock(classToMock, new MockSettings());
    }
    
    /**
     * Creates a mock instance of the given class with specified settings.
     * 
     * @param <T> the type to mock
     * @param classToMock the class to create a mock of
     * @param settings the mock settings
     * @return a mock instance
     */
    public static <T> T mock(Class<T> classToMock, MockSettings settings) {
        // Default implementation uses Mockito adapter for now
        // This will be replaced with our own implementation
        if (!settings.isEnhancedMockEnabled()) {
            return MockitoAdapter.createMock(classToMock);
        }
        
        // Use our own mock creation logic when enhanced mode is enabled
        return MockCreator.createMock(classToMock, settings);
    }
    
    /**
     * Creates a new settings builder for configuring mocks.
     * 
     * @return a new mock settings instance
     */
    public static MockSettings withSettings() {
        return new MockSettings();
    }
    
    /**
     * Prepares for method stubbing or verification.
     * Use this to define behavior for method calls on mocks.
     * 
     * @param <T> the type of the mock
     * @param mock the mock object
     * @return a stub builder
     */
    public static <T> MethodInterceptor<T> when(T mock) {
        return new MethodInterceptor<>(mock);
    }
    
    /**
     * Prepares for method verification.
     * Use this to verify if specific methods were called on mocks.
     * 
     * @param <T> the type of the mock
     * @param mock the mock object
     * @return a verification builder
     */
    public static <T> VerificationBuilder<T> verify(T mock) {
        return new VerificationBuilder<>(mock);
    }
    
    /**
     * Resets a mock to its initial state.
     * 
     * @param <T> the type of the mock
     * @param mock the mock object to reset
     */
    public static <T> void reset(T mock) {
        // Will be implemented later
    }
    
    /**
     * Builder class for verification operations.
     * 
     * @param <T> the type of the mock
     */
    public static class VerificationBuilder<T> {
        private final T mock;
        
        VerificationBuilder(T mock) {
            this.mock = mock;
        }
        
        /**
         * Verifies that a method was called exactly once.
         * 
         * @return the mock object for method call verification
         */
        public T once() {
            // Will be implemented later
            return mock;
        }
        
        /**
         * Verifies that a method was never called.
         * 
         * @return the mock object for method call verification
         */
        public T never() {
            // Will be implemented later
            return mock;
        }
        
        /**
         * Verifies that a method was called exactly the specified number of times.
         * 
         * @param times the number of expected invocations
         * @return the mock object for method call verification
         */
        public T times(int times) {
            // Will be implemented later
            return mock;
        }
        
        /**
         * Returns the mock for method call verification without specific count verification.
         * 
         * @return the mock object
         */
        public T get() {
            return mock;
        }
    }
} 