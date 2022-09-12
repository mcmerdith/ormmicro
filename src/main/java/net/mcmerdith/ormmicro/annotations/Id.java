package net.mcmerdith.ormmicro.annotations;

import java.lang.annotation.*;

/**
 * Specify that this column is the Primary Key for the model
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Id {
    /**
     * If the key should auto-increment
     * <p>Only valid for INTEGER, FLOAT, or DOUBLE types</p>
     */
    boolean autoIncrement() default false;
}
