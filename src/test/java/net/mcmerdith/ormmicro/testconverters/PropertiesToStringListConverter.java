package net.mcmerdith.ormmicro.testconverters;

import net.mcmerdith.ormmicro.typing.AttributeConverter;

import java.util.*;

public class PropertiesToStringListConverter implements AttributeConverter<Properties, List<String>> {
    private static final String keyValueDelimeter = "=";

    @Override
    public List<String> convertToDatabaseColumn(Properties properties) {
        List<String> entries = new ArrayList<>();

        if (properties == null) return entries;

        properties.forEach((k, v) -> entries.add(k + keyValueDelimeter + v));

        return entries;
    }

    @Override
    public Properties convertToModelAttribute(List<String> s) {
        Properties output = new Properties();

        if (s == null || s.isEmpty()) return output;

        for (String entry : s) {
            String[] keyValue = entry.split(keyValueDelimeter);
            if (keyValue.length >= 2) output.setProperty(keyValue[0], keyValue[1]);
        }

        return output;
    }
}
