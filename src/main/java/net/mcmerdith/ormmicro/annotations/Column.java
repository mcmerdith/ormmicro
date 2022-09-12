package net.mcmerdith.ormmicro.annotations;

import net.mcmerdith.ormmicro.typing.ColumnType;
import net.mcmerdith.ormmicro.typing.SqlType;

import java.lang.annotation.*;

/**
 * Define the SQL for the column
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    /**
     * The name of this column in the database
     */
    String name() default "";

    /**
     * The SQL type definition of this column
     */
    SqlType definition() default SqlType.AUTO;

    /**
     * The size of the SQL type (TINY, MEDIUM, BIG, LONG, etc)
     * <p>Only applicable to type INTEGER, TEXT, and BLOB</p>
     * <p>If applicable:<br>
     * Except for type INTEGER, length is ignored<br>
     * precision, digits, and decimals are ignored</p>
     */
    SqlType.Size size() default SqlType.Size.NONE;

    /**
     * A custom column type definition
     * <p>definition, size, length, precision, digits, and decimals will be ignored</p>
     */
    String customDefinition() default "";

    /**
     * The length of the type
     */
    int length() default -1;

    /**
     * The precision of the type
     * Only applies to floating point values
     */
    int precision() default -1;

    /**
     * The number of digits
     * <p>Only applies to DECIMAL type</p>
     */
    int digits() default ColumnType.DEFAULT_DIGITS;

    /**
     * The number of decimals
     * <p>Only applies to DECIMAL type</p>
     */
    int decimals() default ColumnType.DEFAULT_DECIMALS;

    /**
     * If this field is optional (nullable)
     * False: add NOT NULL constraint
     */
    boolean nullable() default true;

    /**
     * If this column must be unique
     */
    boolean unique() default false;

    /**
     * A check clause for this column
     */
    String check() default "";

    /**
     * The default value formatted as a String
     * <p>This value is inserted directly into the SQL statement without escaping, so be cautious</p>
     */
    String defaultValue() default "";
}


