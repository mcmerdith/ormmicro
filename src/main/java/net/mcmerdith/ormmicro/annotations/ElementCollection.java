package net.mcmerdith.ormmicro.annotations;

import java.lang.annotation.*;

/**
 * Specify that this field should be stored in a separate associated table
 * <p>Field must be an instance of {@link java.util.Collection} or an array type</p>
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ElementCollection {
    /**
     * The name of the table the elements will be stored in
     * <p>Prefixed with (parent table)_</p>
     * <p>Default: (column name)</p>
     */
    String associatedTableName() default "";

    /**
     * The name of the column in the associated secondary table
     * <p>Default: (parent table)_id</p>
     */
    String referenceColumnName() default "";

    /**
     * The name of the column that each value will be stored in
     * <p>Default: (column name)</p>
     */
    String valueColumnName() default "";
}
