package com.mocktutorial.core.v3;

/**
 * V3 Mock主API类，暴露mock/when/verify等统一API，屏蔽底层实现。
 */
public class Mock {
    public static <T> T mock(Class<T> clazz) {
        return MockFactory.create(clazz);
    }

    public static <T> WhenBuilder<T> when(T mock, String methodName, Object... args) {
        return new WhenBuilder<>(mock, methodName, args);
    }

    public static <T> VerifyBuilder<T> verify(T mock) {
        return new VerifyBuilder<>(mock);
    }

    // --- 行为定义与验证构建器骨架 ---
    public static class WhenBuilder<T> {
        private final T mock;
        private final String methodName;
        private final Object[] args;
        private Object returnValue;
        private Throwable throwable;
        /**
         * 构造行为定义构建器。
         * @param mock mock对象
         * @param methodName 方法名
         * @param args 方法参数
         */
        public WhenBuilder(T mock, String methodName, Object... args) {
            this.mock = mock;
            this.methodName = methodName;
            this.args = args;
        }
        /**
         * 配置方法返回值。
         */
        public WhenBuilder<T> thenReturn(Object value) {
            this.returnValue = value;
            MockFactory.registerBehavior(mock, new MockFactory.MockBehavior(methodName, args, value, null));
            return this;
        }
        /**
         * 配置方法抛出异常。
         */
        public WhenBuilder<T> thenThrow(Throwable t) {
            this.throwable = t;
            MockFactory.registerBehavior(mock, new MockFactory.MockBehavior(methodName, args, null, t));
            return this;
        }
        // ...
    }
    public static class VerifyBuilder<T> {
        private final T mock;
        private String methodName;
        private Object[] args;
        private int expectedTimes = 1;
        public VerifyBuilder(T mock) {
            this.mock = mock;
        }
        public VerifyBuilder<T> method(String methodName, Object... args) {
            this.methodName = methodName;
            this.args = args;
            return this;
        }
        public VerifyBuilder<T> once() { this.expectedTimes = 1; return check(); }
        public VerifyBuilder<T> times(int n) { this.expectedTimes = n; return check(); }
        private VerifyBuilder<T> check() {
            if (methodName == null) throw new IllegalStateException("请先指定method");
            int actual = MockFactory.countInvocations(mock, methodName, args);
            if (actual != expectedTimes) {
                throw new AssertionError("期望调用 " + methodName + " " + expectedTimes + " 次，实际 " + actual + " 次");
            }
            return this;
        }
        // ...
    }
} 