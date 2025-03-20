package backend.academy.scrapper.build.spring.processors;

import backend.academy.scrapper.build.spring.annotations.SelfAutowired;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

@Component
public class SelfAutowiredBeanPostProcessor implements BeanPostProcessor, Ordered {

    Map<String, Object> map = new HashMap<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Field[] declaredFields = bean.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(SelfAutowired.class)) {
                map.put(beanName, bean);
                break;
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        System.out.println(beanName);
        Object obj = map.get(beanName);
        if (obj != null) {
            Field[] declaredFields = obj.getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                if (field.isAnnotationPresent(SelfAutowired.class)) {
                    field.setAccessible(true);
                    ReflectionUtils.setField(field, obj, bean);
                }
            }
        }
        return bean;
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
