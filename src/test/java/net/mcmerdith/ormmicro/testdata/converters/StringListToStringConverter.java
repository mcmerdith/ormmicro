package net.mcmerdith.ormmicro.testdata.converters;

import net.mcmerdith.ormmicro.typing.AttributeConverter;
import net.mcmerdith.ormmicro.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringListToStringConverter implements AttributeConverter<List<String>, String> {
    private static final String keyValueDelimeter = ";";

    @Override
    public String convertToDatabaseColumn(List<String> properties) {
        if (properties == null) return "";
        return String.join(keyValueDelimeter, properties);
    }

    @Override
    public List<String> convertToModelAttribute(String s) {
        List<String> output = new ArrayList<>();

        if (s == null || StringUtils.isBlank(s)) return output;

        output.addAll(Arrays.asList(s.split(keyValueDelimeter)));

        return output;
    }
}