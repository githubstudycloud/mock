package com.mocktutorial.utils;

import com.mocktutorial.core.Mock;
import com.mocktutorial.core.internal.MockSettings;

/**
 * Helper utilities for working with mocks.
 */
public class MockHelper {
    
    /**
     * Creates a mock with all enhanced features enabled.
     * 
     * @param <T> the type to mock
     * @param classToMock the class to create a mock of
     * @return a fully enhanced mock instance
     */
    public static <T> T createFullyEnhancedMock(Class<T> classToMock) {
        return Mock.mock(classToMock, Mock.withSettings()
                .useEnhancedMock()
                .mockPrivateMethods()
                .mockStaticMethods()
                .mockFinalMethods()
                .mockConstructors());
    }
    
    /**
     * Creates a mock with private method mocking enabled.
     * 
     * @param <T> the type to mock
     * @param classToMock the class to create a mock of
     * @return a mock instance with private method mocking enabled
     */
    public static <T> T createPrivateMethodMock(Class<T> classToMock) {
        return Mock.mock(classToMock, Mock.withSettings()
                .useEnhancedMock()
                .mockPrivateMethods());
    }
    
    /**
     * Creates a mock with static method mocking enabled.
     * 
     * @param <T> the type to mock
     * @param classToMock the class to create a mock of
     * @return a mock instance with static method mocking enabled
     */
    public static <T> T createStaticMethodMock(Class<T> classToMock) {
        return Mock.mock(classToMock, Mock.withSettings()
                .useEnhancedMock()
                .mockStaticMethods());
    }
    
    /**
     * Creates a mock with final method mocking enabled.
     * 
     * @param <T> the type to mock
     * @param classToMock the class to create a mock of
     * @return a mock instance with final method mocking enabled
     */
    public static <T> T createFinalMethodMock(Class<T> classToMock) {
        return Mock.mock(classToMock, Mock.withSettings()
                .useEnhancedMock()
                .mockFinalMethods());
    }
    
    /**
     * Creates a mock with constructor mocking enabled.
     * 
     * @param <T> the type to mock
     * @param classToMock the class to create a mock of
     * @return a mock instance with constructor mocking enabled
     */
    public static <T> T createConstructorMock(Class<T> classToMock) {
        return Mock.mock(classToMock, Mock.withSettings()
                .useEnhancedMock()
                .mockConstructors());
    }
} 