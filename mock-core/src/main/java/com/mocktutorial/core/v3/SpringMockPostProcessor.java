package com.mocktutorial.core.v3;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.Nullable;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;
import com.mocktutorial.core.v3.MockField;

/**
 * V3 Spring集成：自动mock依赖Bean的BeanPostProcessor骨架。
 *
 * 支持：
 * 1. 标注@Mock的字段自动注入mock对象（需自定义@Mock注解）。
 * 2. 可扩展为根据配置/条件自动mock指定Bean。
 */
public class SpringMockPostProcessor implements BeanPostProcessor {
    private static final Set<Class<?>> mockedTypes = new HashSet<>();

    public SpringMockPostProcessor() {
        System.err.println("[SpringMockPostProcessor] Constructor called");
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // 遍历字段，若有@Mock注解则自动注入mock对象
        ReflectionUtils.doWithFields(bean.getClass(), field -> {
            if (field.isAnnotationPresent(MockField.class)) {
                boolean accessible = field.canAccess(bean);
                field.setAccessible(true);
                Object mock = MockFactory.create(field.getType());
                try {
                    field.set(bean, mock);
                    mockedTypes.add(field.getType());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                field.setAccessible(accessible);
            }
        });
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        System.err.println("[SpringMockPostProcessor] postProcessAfterInitialization for bean: " + beanName + ", class: " + bean.getClass().getName());
        // 强制：所有@Mock字段都用mock覆盖
        ReflectionUtils.doWithFields(bean.getClass(), field -> {
            if (field.isAnnotationPresent(MockField.class)) {
                boolean accessible = field.canAccess(bean);
                field.setAccessible(true);
                Object mock = MockFactory.create(field.getType());
                try {
                    System.err.println("[SpringMockPostProcessor] Injecting mock for field '" + field.getName() + "' of type '" + field.getType().getName() + "' in bean '" + beanName + "' -> mock: " + mock);
                    field.set(bean, mock);
                    mockedTypes.add(field.getType());
                    Object after = field.get(bean);
                    System.err.println("[SpringMockPostProcessor] After injection, field value: " + after);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                field.setAccessible(accessible);
            }
        });
        return bean;
    }

    /**
     * 自定义@Mock注解，标记需要自动注入mock的字段。
     */
    public @interface Mock {}
} 