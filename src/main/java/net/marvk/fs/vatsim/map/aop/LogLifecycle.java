package net.marvk.fs.vatsim.map.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LogLifecycle {
    boolean logStart() default true;

    boolean logCompleted() default true;

    boolean logFailed() default true;
}
