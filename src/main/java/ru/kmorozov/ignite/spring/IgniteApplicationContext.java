package ru.kmorozov.ignite.spring;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Created by sbt-morozov-kv on 23.09.2016.
 */
public class IgniteApplicationContext extends AnnotationConfigApplicationContext {

    public IgniteApplicationContext() {
        super(new IgniteBeanFactory());
    }
}
