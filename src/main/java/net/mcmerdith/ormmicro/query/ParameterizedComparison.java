package net.mcmerdith.ormmicro.query;

import net.mcmerdith.ormmicro.OrmMicroLogger;
import net.mcmerdith.ormmicro.SessionFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ParameterizedComparison {
    private final String comparison;
    private final List<Object> parameters = new ArrayList<>();

    public ParameterizedComparison(String comparison, List<Object> parameters) {
        this.comparison = comparison;
        this.parameters.addAll(parameters);
    }

    public String getComparison() {
        return this.comparison;
    }

    public List<Object> getParameters() {
        return this.parameters;
    }

    public void insertParametersInto(PreparedStatement statement) {
        int parameterIndex = 0;

        for (Object parameter : parameters) {
            try {
                statement.setObject(parameterIndex++, parameter);
            } catch (SQLException e) {
                OrmMicroLogger.instance().exception(e, "Failed to insert '" + parameter + "' at index " + (parameterIndex - 1));
            }
        }
    }
}
