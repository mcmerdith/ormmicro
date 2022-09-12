package net.mcmerdith.ormmicro.annotations;

import java.lang.annotation.*;

@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Converts {
    Convert[] value();
}
