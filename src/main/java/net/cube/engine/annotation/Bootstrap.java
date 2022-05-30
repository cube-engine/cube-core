package net.cube.engine.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author pluto
 * @date 2022/5/16
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Bootstrap {

    /**
     *
     * @return
     */
    int priority() default 1;

    /**
     *
     * @return
     */
    boolean singleton() default true;

}
