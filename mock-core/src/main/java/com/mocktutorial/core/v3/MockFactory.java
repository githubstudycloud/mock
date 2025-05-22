package com.mocktutorial.core.v3;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

/**
 * V3 MockFactory: 统一mock对象创建、注册、查找、生命周期管理骨架。
 */
public class MockFactory {
    private static final Map<Class<?>, Object> registry = new ConcurrentHashMap<>();
    private static final Map<Object, List<MockBehavior>> configRegistry = new ConcurrentHashMap<>();
    private static final InstrumentationEngine instrumentationEngine =
            MockAgent.isAvailable() ? new InstrumentationEngine(MockAgent.getInstrumentation()) : null;
    private static final ObjenesisEngine objenesisEngine = new ObjenesisEngine();
    private static final Map<Object, List<InvocationRecord>> invocationRegistry = new ConcurrentHashMap<>();

    public static <T> T create(Class<T> clazz) {
        T mock;
        if (instrumentationEngine != null && instrumentationEngine.isAvailable()) {
            // TODO: InstrumentationEngine创建mock
            mock = null;
        } else {
            // ObjenesisEngine创建mock
            mock = objenesisEngine.createInstance(clazz);
        }
        if (mock != null) {
            registry.put(clazz, mock);
        }
        return mock;
    }

    /**
     * 注册mock行为
     */
    public static void registerBehavior(Object mock, MockBehavior behavior) {
        if (mock == null || behavior == null) return;
        configRegistry.computeIfAbsent(mock, k -> new ArrayList<>()).add(behavior);
    }

    /**
     * 查找mock行为（根据mock、方法名、参数）
     */
    public static MockBehavior findBehavior(Object mock, String methodName, Object[] args) {
        List<MockBehavior> behaviors = configRegistry.get(mock);
        if (behaviors == null) return null;
        for (MockBehavior b : behaviors) {
            if (b.matches(methodName, args)) {
                return b;
            }
        }
        return null;
    }

    @Deprecated
    public static void register(Object mock, Object config) {
        // 兼容旧API，建议用registerBehavior
        if (mock != null && config != null && config instanceof MockBehavior) {
            registerBehavior(mock, (MockBehavior) config);
        }
    }

    public static void clearAll() {
        registry.clear();
        configRegistry.clear();
    }

    public static boolean isInstrumentationAvailable() {
        return instrumentationEngine != null && instrumentationEngine.isAvailable();
    }

    public static boolean isObjenesisAvailable() {
        return objenesisEngine != null;
    }

    /**
     * mock行为描述
     */
    public static class MockBehavior {
        public final String methodName;
        public final Object[] args;
        public final Object returnValue;
        public final Throwable throwable;
        public MockBehavior(String methodName, Object[] args, Object returnValue, Throwable throwable) {
            this.methodName = methodName;
            this.args = args;
            this.returnValue = returnValue;
            this.throwable = throwable;
        }
        public boolean matches(String methodName, Object[] args) {
            if (!Objects.equals(this.methodName, methodName)) return false;
            // 支持无参方法args为null或空数组
            int thisLen = this.args == null ? 0 : this.args.length;
            int thatLen = args == null ? 0 : args.length;
            if (thisLen != thatLen) return false;
            for (int i = 0; i < thisLen; i++) {
                if (!Objects.equals(this.args[i], args[i])) return false;
            }
            return true;
        }
    }

    /**
     * 记录一次mock方法调用
     */
    public static void recordInvocation(Object mock, String methodName, Object[] args) {
        invocationRegistry.computeIfAbsent(mock, k -> new ArrayList<>())
                .add(new InvocationRecord(methodName, args));
    }

    /**
     * 查询mock方法调用次数
     */
    public static int countInvocations(Object mock, String methodName, Object[] args) {
        List<InvocationRecord> list = invocationRegistry.get(mock);
        if (list == null) return 0;
        int count = 0;
        for (InvocationRecord rec : list) {
            if (rec.matches(methodName, args)) count++;
        }
        return count;
    }

    /**
     * 清空所有mock调用记录
     */
    public static void clearInvocations() {
        invocationRegistry.clear();
    }

    /**
     * mock方法调用记录
     */
    public static class InvocationRecord {
        public final String methodName;
        public final Object[] args;
        public InvocationRecord(String methodName, Object[] args) {
            this.methodName = methodName;
            this.args = args == null ? null : args.clone();
        }
        public boolean matches(String methodName, Object[] args) {
            if (!Objects.equals(this.methodName, methodName)) return false;
            int thisLen = this.args == null ? 0 : this.args.length;
            int thatLen = args == null ? 0 : args.length;
            if (thisLen != thatLen) return false;
            for (int i = 0; i < thisLen; i++) {
                if (!Objects.equals(this.args[i], args[i])) return false;
            }
            return true;
        }
    }
} 