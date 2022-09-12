package net.mcmerdith.ormmicro.annotations;

import net.mcmerdith.ormmicro.typing.AttributeConverter;

import java.lang.annotation.*;

/**
 * Apply a type converter to this field
 * <p>Type converters must return a type that can be natively stored in the database</p>
 */
@Documented
@Repeatable(Converts.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Convert {
    /**
     * The converter that should be used
     */
    Class<? extends AttributeConverter<?, ?>> converter();
}
