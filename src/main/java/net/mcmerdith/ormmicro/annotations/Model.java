package net.mcmerdith.ormmicro.annotations;

import java.lang.annotation.*;


@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Model {
    /**
     * The name of the table
     * <p>Default: The name of the class, lowercase</p>
     */
    String tableName() default "";
}
