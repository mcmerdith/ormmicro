package net.mcmerdith.ormmicro.annotations;

import java.lang.annotation.*;

/**
 * Mark this field for exclusion from the database
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Transient {
}
