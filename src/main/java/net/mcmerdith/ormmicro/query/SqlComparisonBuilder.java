package net.mcmerdith.ormmicro.query;

import net.mcmerdith.ormmicro.modeling.ColumnDefinition;
import net.mcmerdith.ormmicro.modeling.MappedSqlModel;
import net.mcmerdith.ormmicro.modeling.SqlModel;
import net.mcmerdith.ormmicro.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class SqlComparisonBuilder {
    private final ComparisonLogic logic;

    private final List<String> comparisons = new ArrayList<>();
    private final List<Object> parameters = new ArrayList<>();

    public SqlComparisonBuilder(ComparisonLogic logic) {
        this.logic = logic;
    }

    /**
     *
     * @param fieldName
     * @param operator
     * @param value
     * @return
     */
    public SqlComparisonBuilder where(String fieldName, ComparisonOperator operator, Object... value) {
        // No column name -> invalid comparison, ignore
        if (StringUtils.isBlank(fieldName)) return this;
        // No comparison -> invalid
        if (operator == null) return this;

        String comparison;

        switch (operator) {
            case BETWEEN:
                if (value.length < 2) return this;
                comparison = " ? AND ?";
                parameters.addAll(Arrays.asList(value[0], value[1]));
                break;
            case EQUAL:
            case LIKE:
            case GREATER:
            case LESS:
            case GREATER_EQUAL:
            case LESS_EQUAL:
            case NOT_EQUAL:
                comparison = " ?";
                parameters.add(value[0]);
                break;
            case IN:
                comparison = " (" + String.join(", ", Collections.nCopies(value.length, "?")) + ")";
                parameters.addAll(Arrays.asList(value));
                break;
            case IS_NULL:
            case NOT_NULL:
            default:
                comparison = "";
                break;
        }

        comparisons.add(String.format("%s %s%s", fieldName, operator.getSql(), comparison));

        return this;
    }

    /**
     * Embed another comparison inside this one
     * @param embeddedComparison The comparison
     */
    public SqlComparisonBuilder where(SqlComparisonBuilder embeddedComparison) {
        if (embeddedComparison == null) return this;

        String comparison = embeddedComparison.buildComparison();
        if (StringUtils.isBlank(comparison)) return this;

        comparisons.add("(" + comparison + ")");
        parameters.addAll(embeddedComparison.parameters);

        return this;
    }

    /**
     * Add all field -> value relations from a model to this query
     * @param model The model to find
     */
    public SqlComparisonBuilder whereMatches(MappedSqlModel<?> model) {
        ColumnDefinition id = model.getModel().getUniqueIdentifier();

        if (id != null) {
            return where(id.getName(), ComparisonOperator.EQUAL, model.getColumns().get(id.getField().getName()));
        }

        Map<String, ColumnDefinition> columnDef = model.getModel().getColumnDefinitions();

        for (Map.Entry<String, Object> values : model.getColumns().entrySet()) {
            ColumnDefinition column = columnDef.get(values.getKey());
            where(column.getName(), ComparisonOperator.EQUAL, values.getValue());
        }

        return this;
    }

    /**
     * Find rows where the ID matches a value
     * @param model The model to find
     * @param value The ID value to check
     */
    public SqlComparisonBuilder whereIdMatch(SqlModel<?> model, Object value) {
        if (model == null || model.getUniqueIdentifier() == null) return this;

        return where(model.getUniqueIdentifier().getName(), ComparisonOperator.EQUAL, value);
    }

    private String buildComparison() {
        return String.join(" " + logic.name() + " ", comparisons);
    }

    public ParameterizedComparison build() {
        return new ParameterizedComparison(buildComparison(), parameters);
    }
}
