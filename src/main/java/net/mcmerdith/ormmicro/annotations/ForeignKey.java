package net.mcmerdith.ormmicro.annotations;

import java.lang.annotation.*;

/**
 * Specify that this field should be stored in a separate associated table
 * <p>Field type must be a {@link Model}</p>
 */
@Documented
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ForeignKey {
    /**
     * A `TABLE(COLUMN)` definition for a table not tracked by the ORM
     * <p>Optional: If this annotation is applied to a field who's type represents a SqlModel the reference
     * is calculated automatically</p>
     */
    String externalReference() default "";
}
