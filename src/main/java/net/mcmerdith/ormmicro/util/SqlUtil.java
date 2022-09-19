package net.mcmerdith.ormmicro.util;

import net.mcmerdith.ormmicro.OrmMicroLogger;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class SqlUtil {
    public static void insertParametersInto(PreparedStatement statement, List<Object> parameters) {
        int parameterIndex = 0;

        for (Object parameter : parameters) {
            try {
                statement.setObject(parameterIndex++, parameter);
            } catch (SQLException e) {
                OrmMicroLogger.QUERY_BUILDER.exception(e, "Failed to insert '" + parameter + "' at index " + (parameterIndex - 1));
            }
        }
    }
}
