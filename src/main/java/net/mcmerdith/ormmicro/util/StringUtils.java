package net.mcmerdith.ormmicro.util;

public class StringUtils {
    private StringUtils() {
        // Don't instantiate this class
    }

    private static final char cPropertyDelimiter = '=';
    private static final char cDelimiter = ';';
    public static final String propertyDelimiter = String.valueOf(cPropertyDelimiter);
    public static final String delimiter = String.valueOf(cDelimiter);

    public static String escape(String toEscape, char... escapedCharacters) {
        String current = toEscape;

        for (char c : escapedCharacters) {
            current = current.replaceAll(String.valueOf(c), "&" + ((int) c) + ";");
        }

        return current;
    }

    public static String unescape(String toUnescape, char... escapedCharacters) {
        String current = toUnescape;

        for (char c : escapedCharacters) {
            current = current.replaceAll("&" + ((int) c) + ";", String.valueOf(c));
        }

        return current;
    }

    public static String escapeProperties(String properties) {
        return escape(properties, cPropertyDelimiter, cDelimiter);
    }

    public static String unescapeProperties(String properties) {
        return unescape(properties, cPropertyDelimiter, cDelimiter);
    }

    public static boolean isBlank(String string) {
        return string == null || string.isBlank();
    }

    public static boolean notBlank(String string) {
        return string != null && !string.isBlank();
    }
}
