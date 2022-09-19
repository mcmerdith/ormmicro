package net.mcmerdith.ormmicro.query;

import net.mcmerdith.ormmicro.OrmMicroLogger;
import net.mcmerdith.ormmicro.internal.Session;
import net.mcmerdith.ormmicro.modeling.ColumnDefinition;
import net.mcmerdith.ormmicro.modeling.ElementCollectionTable;
import net.mcmerdith.ormmicro.modeling.SqlModel;
import net.mcmerdith.ormmicro.query.comparison.ComparisonLogic;
import net.mcmerdith.ormmicro.query.comparison.ComparisonOperator;
import net.mcmerdith.ormmicro.query.comparison.ParameterizedComparison;
import net.mcmerdith.ormmicro.query.comparison.SqlComparisonBuilder;
import net.mcmerdith.ormmicro.util.SqlUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SqlQuery {

    private final Session session;

    private final List<String> columns = new ArrayList<>();

    private ParameterizedComparison where = null;

    private boolean distinct = false;
    private int limit = Integer.MAX_VALUE;
    private final Map<String, ColumnOrder> orderBy = new LinkedHashMap<>();
    private boolean min = false;
    private boolean max = false;
    private boolean count = false;
    private boolean average = false;
    private boolean sum = false;

    /**
     * Construct a SQL query
     * <p>Base Query: `SELECT * FROM [modelClassTable]`</p>
     *
     * @param modelClass The model to retreive
     */
    public SqlQuery(Session session) {
        this.session = session;
    }

    /**
     * Select only certain columns
     *
     * @param columns The columns to select
     */
    public SqlQuery select(String... columns) {
        this.columns.addAll(Arrays.asList(columns));
        return this;
    }

    /**
     * Only find models matching this condition
     *
     * @param comparison The comparison to match against
     */
    public SqlQuery where(ParameterizedComparison comparison) {
        this.where = comparison;
        return this;
    }

    public SqlQuery orderBy(String column, ColumnOrder order) {
        orderBy.put(column, order);
        return this;
    }

    private void updateFunction(boolean distinct, int limit, boolean min, boolean max, boolean count, boolean average, boolean sum) {
        this.distinct = distinct;
        this.limit = limit;
        this.min = min;
        this.max = max;
        this.count = count;
        this.average = average;
        this.sum = sum;
    }

    public SqlQuery distinct() {
        updateFunction(true, limit, false, false, false, false, false);
        return this;
    }

    public SqlQuery limit(int limit) {
        updateFunction(distinct, limit, false, false, false, false, false);
        return this;
    }

    public SqlQuery min() {
        updateFunction(false, Integer.MAX_VALUE, true, false, false, false, false);
        return this;
    }

    public SqlQuery max() {
        updateFunction(false, Integer.MAX_VALUE, false, true, false, false, false);
        return this;
    }

    public SqlQuery count() {
        updateFunction(false, Integer.MAX_VALUE, false, false, true, false, false);
        return this;
    }

    public SqlQuery average() {
        updateFunction(false, Integer.MAX_VALUE, false, false, false, true, false);
        return this;
    }

    public SqlQuery sum() {
        updateFunction(false, Integer.MAX_VALUE, false, false, false, false, true);
        return this;
    }

    /**
     * Format: Columns, Table, Where, Order, (Limit)
     */
    private static final String selectFormat = "SELECT %s FROM %s%s%s%s";

    /**
     * Format: Function, Column
     */
    private static final String functionFormat = "%s(%s)";

    private String buildSql(SqlModel<?> model) {
        return buildSql(
                model.getTableName(),
                model.getColumnDefinitions().values().stream()
                        .map(ColumnDefinition::getName)
                        .toArray(String[]::new)
        );
    }

    private String buildSql(String table, String... defaultColumns) {
        // Sql functions need at least one column
        String where = (this.where == null) ? "" : " WHERE " + this.where.getComparison();
        String order = (orderBy.isEmpty()) ? "" : " ORDER BY " + orderBy.entrySet().stream().map(
                e -> e.getKey() + " " + e.getValue().name()
        ).collect(Collectors.joining(", "));
        String limit = (this.limit == Integer.MAX_VALUE) ? "" : " LIMIT " + this.limit;

        String selector = "";
        if (min || max || count || average || sum) {
            if (columns.isEmpty()) return null;
            String column = columns.get(0);

            if (min) {
                selector = String.format(functionFormat, "MIN", column);
            } else if (max) {
                selector = String.format(functionFormat, "MAX", column);
            } else if (count) {
                selector = String.format(functionFormat, "COUNT", column);
            } else if (average) {
                selector = String.format(functionFormat, "AVG", column);
            } else {
                selector = String.format(functionFormat, "SUM", column);
            }
        } else {
            if (columns.isEmpty())
                if (defaultColumns.length == 0) {
                    columns.add("*");
                } else {
                    columns.addAll(Arrays.asList(defaultColumns));
                }
            if (distinct) selector = "DISTINCT ";
            // TODO Sql Server puts "limit" here
            selector += String.join(", ", columns);
        }

        return String.format(selectFormat, selector, table, where, order, limit);
    }

    public ResultSet execute(String table) {
        String statement = buildSql(table);

        try {
            return executeStatement(statement, where.getParameters());
        } catch (SQLException e) {
            OrmMicroLogger.QUERY_BUILDER.exception(e, "Failed to execute query `" + statement + "`");
        }

        return null;
    }

    public void executeAsync(String table, Consumer<ResultSet> consumer) {
        session.getFactory().getWorker().executeQueryAsync(
                buildSql(table),
                where.getParameters(),
                consumer
        );
    }

    public <T> QueryResult<T> execute(Class<T> modelClass) {
        // TODO dialect
        SqlModel<T> model = session.getFactory().getModelManager().getModel(modelClass);
        String statement = buildSql(model);
        try {
            return processResults(
                    modelClass,
                    model,
                    session,
                    executeStatement(statement, where.getParameters())
            );
        } catch (SQLException e) {
            OrmMicroLogger.QUERY_BUILDER.exception(e, "Failed to execute query `" + statement + "`");
        }

        return null;
    }

    public <T> void executeAsync(Class<T> modelClass, Consumer<QueryResult<T>> consumer) {
        SqlModel<T> model = session.getFactory().getModelManager().getModel(modelClass);
        session.getFactory().getWorker().executeQueryAsync(
                buildSql(model),
                where.getParameters(),
                rs -> {
                    try (Session newSession = session.getFactory().getCurrentSession()) {
                        consumer.accept(processResults(modelClass, model, newSession, rs));
                    }
                }
        );
    }

    private ResultSet executeStatement(String statement, List<Object> parameters) throws SQLException {
        PreparedStatement pStatement = session.getConnection().prepareStatement(statement);
        SqlUtil.insertParametersInto(pStatement, parameters);

        return pStatement.executeQuery();
    }

    private <T> QueryResult<T> processResults(Class<T> modelClass, SqlModel<T> model, Session session, ResultSet rs) {
        List<T> results = new ArrayList<>();

        if (rs != null) {
            try {
                // Process the resultset
                Constructor<T> instanceBuilder = modelClass.getConstructor();
                instanceBuilder.setAccessible(true);

                while (rs.next()) {
                    T next = instanceBuilder.newInstance();

                    Map<String, ColumnDefinition> definitions = model.getColumnDefinitions();

                    Map<String, ElementCollectionTable<?>> associatedCollections = new HashMap<>();

                    for (var definition : definitions.entrySet()) {
                        String fieldName = definition.getKey();
                        ColumnDefinition column = definition.getValue();

                        List<Object> associatedObjects = new ArrayList<>();

                        if (column.isArray() || column.isCollection()) {
                            // We'll need to get the elements from an associated table
                            associatedCollections.put(fieldName, column.getCollectionTable());
                            continue;
                        }

                        if (column.isForeignKey()) {
                            column.foreign
                        }

                        Object db = column.convertSqlToJava(rs.getObject(column.getName(), column.getFieldType()));
                    }

                    for (var collection : associatedCollections.entrySet()) {
                        String fieldName = collection.getKey();
                        ElementCollectionTable<?> collectionTable = collection.getValue();
                        ColumnDefinition definition = definitions.get(fieldName);
                    }
                }
            } catch (SQLException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                OrmMicroLogger.DATABASE_WORKER.exception(e, "Failed to get next query result");
            }
        }

        return new QueryResult<>(modelClass, results);
    }

    private <T> T getForeignObject(Class<T> type, ResultSet data, ) {
        foreignColumn.get
    }
}
