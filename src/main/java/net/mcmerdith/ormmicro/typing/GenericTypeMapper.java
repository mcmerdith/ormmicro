package net.mcmerdith.ormmicro.typing;

import java.util.HashMap;
import java.util.Map;

/**
 * Map Java types to SQL types and vice versa
 */
public class GenericTypeMapper implements ISqlTypeMapper {

    private static final Map<Class<?>, ColumnType> equivalent = new HashMap<>();

    static {
        // Strings
        equivalent.put(String.class, ColumnType.STRING);

        // Primitives need both their wrapper and primitive class registered
        equivalent.put(Byte.class, ColumnType.TINYINT);
        equivalent.put(Byte.TYPE, ColumnType.TINYINT);

        equivalent.put(Short.class, ColumnType.MEDIUMINT);
        equivalent.put(Short.TYPE, ColumnType.MEDIUMINT);

        equivalent.put(Integer.class, ColumnType.INTEGER);
        equivalent.put(Integer.TYPE, ColumnType.INTEGER);

        equivalent.put(Long.class, ColumnType.BIGINT);
        equivalent.put(Long.TYPE, ColumnType.BIGINT);

        equivalent.put(Float.class, ColumnType.FLOAT);
        equivalent.put(Float.TYPE, ColumnType.FLOAT);

        equivalent.put(Double.class, ColumnType.DOUBLE);
        equivalent.put(Double.TYPE, ColumnType.DOUBLE);

        equivalent.put(Boolean.class, ColumnType.BOOLEAN);
        equivalent.put(Boolean.TYPE, ColumnType.BOOLEAN);
    }

    /**
     * Get the SQL type best representing a Java object
     *
     * @param java The object to get the type of
     * @return The SQL type, or null if a suitable match cannot be found
     */
    public ColumnType javaToSqlType(Class<?> java) {
        return equivalent.get(java);
    }
}
