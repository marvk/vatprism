package net.marvk.fs.vatsim.map.view.painter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Parameter {
    String value();

    String bind() default "";

    boolean visible() default true;

    double min() default -Double.MAX_VALUE;

    double max() default Double.MAX_VALUE;
}
