package net.mcmerdith.ormmicro.query.comparison;

import net.mcmerdith.ormmicro.OrmMicroLogger;

import java.sql.PreparedStatement;
import java.sql.SQLException;
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
}
