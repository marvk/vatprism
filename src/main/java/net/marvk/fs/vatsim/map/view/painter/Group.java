package net.marvk.fs.vatsim.map.view.painter;

import kotlin.Deprecated;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Deprecated(message = "Removed with preferences 2")
public @interface Group {
    String value() default "";
}
