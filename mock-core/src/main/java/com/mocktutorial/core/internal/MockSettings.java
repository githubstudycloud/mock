package com.mocktutorial.core.internal;

/**
 * 【已更新V2】
 * mock对象的配置项，支持链式调用，控制是否启用增强mock、静态/私有/构造函数/final方法mock等。
 * <ul>
 *   <li>所有配置项均为本mock实例独立。</li>
 *   <li>支持JDK21和JDK8+，可指定自定义ClassLoader。</li>
 * </ul>
 * <p>
 * 典型用法：
 * <pre>
 *   MockSettings settings = new MockSettings()
 *       .useEnhancedMock()
 *       .mockStaticMethods()
 *       .mockPrivateMethods();
 *   UserService mock = Mock.mock(UserService.class, settings);
 * </pre>
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
     * 【已更新V2】
     * 启用增强mock能力（如字节码增强、静态/私有/构造函数mock等）。
     * @return this，支持链式调用
     */
    public MockSettings useEnhancedMock() {
        this.enhancedMockEnabled = true;
        return this;
    }
    
    /**
     * 【已更新V2】
     * 启用私有方法mock。
     * @return this，支持链式调用
     */
    public MockSettings mockPrivateMethods() {
        this.mockPrivateMethods = true;
        return this;
    }
    
    /**
     * 【已更新V2】
     * 启用静态方法mock。
     * @return this，支持链式调用
     */
    public MockSettings mockStaticMethods() {
        this.mockStaticMethods = true;
        return this;
    }
    
    /**
     * 【已更新V2】
     * 启用final方法mock。
     * @return this，支持链式调用
     */
    public MockSettings mockFinalMethods() {
        this.mockFinalMethods = true;
        return this;
    }
    
    /**
     * 【已更新V2】
     * 启用构造函数mock。
     * @return this，支持链式调用
     */
    public MockSettings mockConstructors() {
        this.mockConstructors = true;
        return this;
    }
    
    /**
     * 【已更新V2】
     * 设置mock名称（便于调试和日志）。
     * @param name mock名称
     * @return this，支持链式调用
     */
    public MockSettings name(String name) {
        this.name = name;
        return this;
    }
    
    /**
     * 【已更新V2】
     * 设置自定义ClassLoader（高级用法）。
     * @param classLoader 类加载器
     * @return this，支持链式调用
     */
    public MockSettings classLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }
    
    /**
     * 【已更新V2】
     * 是否启用增强mock。
     * @return true=启用
     */
    public boolean isEnhancedMockEnabled() {
        return enhancedMockEnabled;
    }
    
    /**
     * 【已更新V2】
     * 是否启用私有方法mock。
     * @return true=启用
     */
    public boolean isMockPrivateMethodsEnabled() {
        return mockPrivateMethods;
    }
    
    /**
     * 【已更新V2】
     * 是否启用静态方法mock。
     * @return true=启用
     */
    public boolean isMockStaticMethodsEnabled() {
        return mockStaticMethods;
    }
    
    /**
     * 【已更新V2】
     * 是否启用final方法mock。
     * @return true=启用
     */
    public boolean isMockFinalMethodsEnabled() {
        return mockFinalMethods;
    }
    
    /**
     * 【已更新V2】
     * 是否启用构造函数mock。
     * @return true=启用
     */
    public boolean isMockConstructorsEnabled() {
        return mockConstructors;
    }
    
    /**
     * 【已更新V2】
     * 获取mock名称。
     * @return mock名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 【已更新V2】
     * 获取mock的ClassLoader。
     * @return 类加载器
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }
} 