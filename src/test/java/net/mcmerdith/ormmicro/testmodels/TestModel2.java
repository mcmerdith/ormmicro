package net.mcmerdith.ormmicro.testmodels;

import net.mcmerdith.ormmicro.annotations.Convert;
import net.mcmerdith.ormmicro.annotations.ElementCollection;
import net.mcmerdith.ormmicro.annotations.Id;
import net.mcmerdith.ormmicro.testconverters.PropertiesToStringListConverter;
import net.mcmerdith.ormmicro.testconverters.StringListToStringConverter;

import java.math.BigDecimal;
import java.util.Properties;

public class TestModel2 {
    @Id
    private long field1;
    private boolean field2;
    private BigDecimal field3;
    @Convert(converter = PropertiesToStringListConverter.class)
    @Convert(converter = StringListToStringConverter.class)
    private Properties field4;

    private TestModel2() {}

    public TestModel2(boolean field2, BigDecimal field3, Properties field4) {
        this.field2 = field2;
        this.field3 = field3;
        this.field4 = field4;
    }
}
