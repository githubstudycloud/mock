package com.mocktutorial.core.v3;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * V3接口mock主链路测试：验证when/thenReturn/thenThrow生效。
 */
public class MockInterfaceTest {
    static class SampleClass {
        public String hello(String name) { return "real-" + name; }
        public int add(int a, int b) { return a + b; }
        public void fail() { throw new RuntimeException("real fail"); }
    }

    @Test
    void testWhenThenReturn() {
        SampleService mock = Mock.mock(SampleService.class);
        Mock.when(mock, "hello", "world").thenReturn("hi, world");
        Mock.when(mock, "add", 1, 2).thenReturn(42);
        assertEquals("hi, world", mock.hello("world"), "hello方法应返回mock值");
        assertEquals(42, mock.add(1, 2), "add方法应返回mock值");
        assertNull(mock.hello("other"), "未mock参数应返回null");
    }

    @Test
    void testWhenThenThrow() {
        SampleService mock = Mock.mock(SampleService.class);
        Mock.when(mock, "fail").thenThrow(new IllegalStateException("fail!"));
        Exception ex = assertThrows(IllegalStateException.class, mock::fail, "应抛出mock异常");
        assertEquals("fail!", ex.getMessage());
    }

    @Test
    void testClassMockWhenThenReturn() {
        SampleClass mock = Mock.mock(SampleClass.class);
        Mock.when(mock, "hello", "world").thenReturn("hi, world");
        Mock.when(mock, "add", 1, 2).thenReturn(42);
        assertEquals("hi, world", mock.hello("world"), "hello方法应返回mock值");
        assertEquals(42, mock.add(1, 2), "add方法应返回mock值");
        assertNull(mock.hello("other"), "未mock参数应返回null");
    }

    @Test
    void testClassMockWhenThenThrow() {
        SampleClass mock = Mock.mock(SampleClass.class);
        Mock.when(mock, "fail").thenThrow(new IllegalStateException("fail!"));
        Exception ex = assertThrows(IllegalStateException.class, mock::fail, "应抛出mock异常");
        assertEquals("fail!", ex.getMessage());
    }

    @Test
    void testInterfaceVerify() {
        SampleService mock = Mock.mock(SampleService.class);
        Mock.when(mock, "hello", "world").thenReturn("hi, world");
        mock.hello("world");
        mock.hello("world");
        Mock.verify(mock).method("hello", "world").times(2);
        assertThrows(AssertionError.class, () -> Mock.verify(mock).method("hello", "world").once());
    }

    @Test
    void testClassVerify() {
        SampleClass mock = Mock.mock(SampleClass.class);
        Mock.when(mock, "add", 1, 2).thenReturn(42);
        mock.add(1, 2);
        Mock.verify(mock).method("add", 1, 2).once();
        assertThrows(AssertionError.class, () -> Mock.verify(mock).method("add", 1, 2).times(2));
    }

    @Configuration
    static class SpringConfig {
        @Bean
        public SpringMockPostProcessor springMockPostProcessor() {
            return new SpringMockPostProcessor();
        }
        @Bean
        public SpringBean springBean() {
            return new SpringBean();
        }
    }
    @Test
    void testSpringMockInjection() {
        System.err.println("[Test] Before context refresh");
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
        System.err.println("[Test] After context refresh");
        SpringBean bean = ctx.getBean(SpringBean.class);
        System.err.println("[Test] Got SpringBean: " + bean);
        assertNotNull(bean.sampleService, "sampleService should be injected by mock processor");
        Mock.when(bean.sampleService, "hello", "spring").thenReturn("hi, spring");
        assertEquals("hi, spring", bean.call("spring"));
        ctx.close();
    }
} 