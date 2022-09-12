package net.mcmerdith.ormmicro.annotations;

import net.mcmerdith.ormmicro.typing.ColumnType;
import net.mcmerdith.ormmicro.typing.SqlType;

import java.lang.annotation.*;

/**
 * Specify how Java Enums should be stored
 * Only applicable to fields with a Java Enum type
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumStorage {
    enum Mode {
        /**
         * Store Enums by ordinal number
         */
        ORDINAL(ColumnType.INTEGER),
        /**
         * Store Enums by their name()
         */
        VALUE(ColumnType.STRING);

        private final ColumnType sqlType;

        public ColumnType getType() {
            return this.sqlType;
        }

        Mode(ColumnType sqlType) {
            this.sqlType = sqlType;
        }
    }

    /**
     * How enum values should be stored
     * <p>Default: {@link Mode#VALUE}</p>
     */
    Mode mode() default Mode.VALUE;
}
