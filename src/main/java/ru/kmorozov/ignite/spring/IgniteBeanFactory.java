package ru.kmorozov.ignite.spring;

import org.apache.ignite.Ignite;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.kmorozov.ignite.spring.annotations.IgniteResource;

import java.util.Set;

/**
 * Created by sbt-morozov-kv on 22.09.2016.
 */
public class IgniteBeanFactory extends DefaultListableBeanFactory {

    private IgniteSpringBootConfiguration configuration;

    @Override
    public Object resolveDependency(DependencyDescriptor descriptor, String beanName,
                                    Set<String> autowiredBeanNames,
                                    TypeConverter typeConverter) throws BeansException {
        if (descriptor == null
                || descriptor.getField() == null
                || !descriptor.getField().getType().equals(Ignite.class))
            return super.resolveDependency(descriptor, beanName,
                    autowiredBeanNames, typeConverter);
        else {
            if (configuration == null)
                configuration = new IgniteSpringBootConfiguration(
                        createBean(DefaultIgniteProperties.class));
            return configuration.getIgnite(
                    descriptor.getField().getAnnotationsByType(IgniteResource.class));
        }
    }
}
