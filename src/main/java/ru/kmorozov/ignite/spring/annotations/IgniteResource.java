package ru.kmorozov.ignite.spring.annotations;

import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by sbt-morozov-kv on 23.09.2016.
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Autowired
public @interface IgniteResource {

    String gridName() default "";

    boolean clientMode() default true;

    boolean peerClassLoadingEnabled() default true;

    String localAddress() default "";

    String ipDiscoveryRange() default "";

    boolean createIfNotExists() default true;
}
