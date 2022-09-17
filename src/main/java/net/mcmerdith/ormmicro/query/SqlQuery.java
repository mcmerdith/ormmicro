package net.mcmerdith.ormmicro.query;

import net.mcmerdith.ormmicro.Session;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SqlQuery<T> {
    private final Class<T> modelClass;

    private final List<String> columns = new ArrayList<>();

    private ParameterizedComparison where = null;

    private boolean distinct = false;
    private int limit = Integer.MAX_VALUE;
    private Map<String, ColumnOrder> orderBy = new LinkedHashMap<>();
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
    public SqlQuery(Class<T> modelClass) {
        this.modelClass = modelClass;
    }

    /**
     * Select only certain columns
     *
     * @param columns The columns to select
     */
    public SqlQuery<T> select(String... columns) {
        this.columns.addAll(Arrays.asList(columns));
        return this;
    }

    /**
     * Only find models matching this condition
     *
     * @param comparison The comparison to match against
     */
    public SqlQuery<T> where(ParameterizedComparison comparison) {
        this.where = comparison;
        return this;
    }

    public SqlQuery<T> orderBy(String column, ColumnOrder order) {
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

    public SqlQuery<T> distinct() {
        updateFunction(true, limit, false, false, false, false, false);
        return this;
    }

    public SqlQuery<T> limit(int limit) {
        updateFunction(distinct, limit, false, false, false, false, false);
        return this;
    }

    public SqlQuery<T> min() {
        updateFunction(false, Integer.MAX_VALUE, true, false, false, false, false);
        return this;
    }

    public SqlQuery<T> max() {
        updateFunction(false, Integer.MAX_VALUE, false, true, false, false, false);
        return this;
    }

    public SqlQuery<T> count() {
        updateFunction(false, Integer.MAX_VALUE, false, false, true, false, false);
        return this;
    }

    public SqlQuery<T> average() {
        updateFunction(false, Integer.MAX_VALUE, false, false, false, true, false);
        return this;
    }

    public SqlQuery<T> sum() {
        updateFunction(false, Integer.MAX_VALUE, false, false, false, false, true);
        return this;
    }

    public QueryResult<T> executeNow(Session session) {
        return null;
    }

    public void executeLater(Session session, Consumer<QueryResult<T>> consumer) {

    }
}
