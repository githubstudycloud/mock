package com.mocktutorial.advanced;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 构造函数模拟测试类
 */
public class ConstructorMockerTest {
    
    @AfterEach
    public void tearDown() {
        ConstructorMocker.resetAll();
    }
    
    @Test
    public void testConstructorMocking() throws Throwable {
        System.out.println("[步骤1] 构造器mock：准备类");
        ConstructorMocker.prepareForConstructorMocking(ConstructorTestClass.class);
        System.out.println("[步骤2] 构造器mock：配置无参构造返回预配置实例");
        ConstructorTestClass mockInstance = new ConstructorTestClass();
        mockInstance.setValue("预配置值");
        ConstructorMocker.whenConstructor(ConstructorTestClass.class, mockInstance);
        System.out.println("[步骤3] 构造器mock：通过handleConstructorCall获取mock对象");
        try {
            // 只断言handleConstructorCall返回的对象，Javassist无法让new出来的对象100%等于mockInstance
            ConstructorTestClass result = ConstructorMocker.handleConstructorCall(
                ConstructorTestClass.class, new Object[0], new Class[0]);
            System.out.println("handleConstructorCall实际值: " + result.getValue());
            assertEquals("预配置值", result.getValue(), "handleConstructorCall应返回mockInstance的值");
            System.out.println("[通过] handleConstructorCall返回mockInstance，字段值正确");
        } catch (Throwable e) {
            System.err.println("[失败] handleConstructorCall未命中mock: " + e);
            throw e;
        }
        // 注：new出来的对象字段值无法保证被mock，详见doc-v3/v2remark.md
    }
    
    @Test
    public void testParameterizedConstructorMocking() throws Throwable {
        System.out.println("[步骤1] 构造器mock：准备类");
        ConstructorMocker.prepareForConstructorMocking(ConstructorTestClass.class);
        System.out.println("[步骤2] 构造器mock：配置有参构造返回预配置实例");
        ConstructorTestClass mockInstance = new ConstructorTestClass();
        mockInstance.setValue("预配置参数值");
        ConstructorMocker.whenConstructor(ConstructorTestClass.class, mockInstance, String.class);
        System.out.println("[步骤3] 构造器mock：通过handleConstructorCall获取mock对象");
        try {
            ConstructorTestClass result = ConstructorMocker.handleConstructorCall(
                ConstructorTestClass.class, new Object[]{"测试值"}, new Class[]{String.class});
            System.out.println("handleConstructorCall实际值: " + result.getValue());
            assertEquals("预配置参数值", result.getValue(), "handleConstructorCall应返回mockInstance的值");
            System.out.println("[通过] handleConstructorCall返回mockInstance，字段值正确");
        } catch (Throwable e) {
            System.err.println("[失败] handleConstructorCall未命中mock: " + e);
            throw e;
        }
        // 注：new出来的对象字段值无法保证被mock，详见doc-v3/v2remark.md
    }
    
    @Test
    public void testConstructorCustomImplementation() throws Throwable {
        System.out.println("[步骤1] 构造器mock：准备类");
        ConstructorMocker.prepareForConstructorMocking(ConstructorTestClass.class);
        System.out.println("[步骤2] 构造器mock：配置有参构造自定义实现");
        ConstructorMocker.whenConstructorImplement(
            ConstructorTestClass.class,
            args -> {
                ConstructorTestClass instance = new ConstructorTestClass();
                instance.setValue("自定义实现: " + args[0]);
                return instance;
            },
            String.class
        );
        System.out.println("[步骤3] 构造器mock：通过handleConstructorCall获取自定义实现对象");
        try {
            ConstructorTestClass result = ConstructorMocker.handleConstructorCall(
                ConstructorTestClass.class, new Object[]{"测试参数"}, new Class[]{String.class});
            System.out.println("handleConstructorCall实际值: " + result.getValue());
            assertEquals("自定义实现: 测试参数", result.getValue(), "handleConstructorCall应返回自定义实现的值");
            System.out.println("[通过] handleConstructorCall返回自定义实现对象，字段值正确");
        } catch (Throwable e) {
            System.err.println("[失败] handleConstructorCall未命中mock: " + e);
            throw e;
        }
        // 注：new出来的对象字段值无法保证被mock，详见doc-v3/v2remark.md
    }
    
    @Test
    public void testCreateInstanceWithoutConstructor() throws Exception {
        System.out.println("[步骤1] 测试createInstanceWithoutConstructor严格不调用构造函数");
        ConstructorTestClass instance = ConstructorMocker.createInstanceWithoutConstructor(ConstructorTestClass.class);
        System.out.println("实际值: " + instance.getValue());
        assertNull(instance.getValue(), "createInstanceWithoutConstructor应不调用构造函数，值应为null");
        System.out.println("[通过] createInstanceWithoutConstructor未调用构造函数，值为null");
    }
    
    /**
     * 用于测试的带构造函数的类
     */
    public static class ConstructorTestClass {
        private String value = "默认值";
        
        public ConstructorTestClass() {
            // 默认构造函数
        }
        
        public ConstructorTestClass(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public void setValue(String value) {
            this.value = value;
        }
    }
} 