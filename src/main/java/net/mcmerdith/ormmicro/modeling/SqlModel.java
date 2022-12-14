package net.mcmerdith.ormmicro.modeling;

import net.mcmerdith.ormmicro.OrmMicroLogger;
import net.mcmerdith.ormmicro.annotations.Model;
import net.mcmerdith.ormmicro.internal.SessionFactory;
import net.mcmerdith.ormmicro.util.StringUtils;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

public class SqlModel<T> {
    private final SessionFactory session;
    /**
     * The name of the table this model belongs to
     */
    private String tableName = null;

    /**
     * The SQL definitions for the columns
     * Mapped as: columnName -> columnDefinition
     */
    private final Map<String, ColumnDefinition> columnDefinitions = new LinkedHashMap<>();

    public SqlModel(SessionFactory session, Class<T> modelClass) {
        this.session = session;

        // Check for a @Model annotation. Not required, but does change the table name
        Model modelMeta = modelClass.getDeclaredAnnotation(Model.class);

        if (modelMeta != null) {
            // Get the name from the annotation
            tableName = modelMeta.tableName();
        }

        if (StringUtils.isBlank(tableName)) {
            // Use the class name if we didn't define a name
            tableName = modelClass.getSimpleName().toLowerCase();
        }

        Map<String, ColumnDefinition> tempColumns = new LinkedHashMap<>();

        for (Field field : modelClass.getDeclaredFields()) {
            ColumnDefinition tempColumn = processAnnotatedField(field);
            if (tempColumn != null) tempColumns.put(field.getName(), tempColumn);
        }

        // Sort the columns, putting primary and unique keys first
        columnDefinitions.putAll(
                tempColumns.entrySet().stream()
                        .sorted(
                                Comparator.comparing(
                                        (Map.Entry<String, ColumnDefinition> e) -> e.getValue().isPrimary()
                                ).thenComparing(
                                        e -> e.getValue().isUnique()
                                )
                        )
                        .collect(
                                LinkedHashMap<String, ColumnDefinition>::new,
                                (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                                LinkedHashMap::putAll
                        )
        );

        if (columnDefinitions.values().stream().filter(ColumnDefinition::isPrimary).count() > 1) {
            throw OrmMicroLogger.MODEL_MAPPER.exception(null, "Model " + tableName + " has more than 1 primary key!", true);
        }
    }

    /*
    Schema Management
     */

    /**
     * Read the annotations on a field
     *
     * @param annotated The annotated field
     */
    private ColumnDefinition processAnnotatedField(Field annotated) {
        ColumnDefinition column = new ColumnDefinition(session, this, annotated);

        // Don't track transient columns
        if (column.isTransient()) return null;

        return column;
    }

    public String getTableName() {
        return getTableName(false);
    }

    protected String getTableName(boolean raw) {
        return raw ? tableName : session.getNameManager().applyStrategiesForTable(tableName);
    }

    /**
     * Get the column that uniquely identifies this model
     *
     * @return The Primary Key (if present) or the most suitable Unique Key, else null
     */
    public ColumnDefinition getUniqueIdentifier() {
        return columnDefinitions.values().stream().filter(ColumnDefinition::isPrimary).findFirst().orElseGet(
                () -> columnDefinitions.values().stream().filter(ColumnDefinition::isUnique).findFirst().orElse(null)
        );
    }

    public Map<String, ColumnDefinition> getColumnDefinitions() {
        return new LinkedHashMap<>(columnDefinitions);
    }

    public ColumnDefinition getColumnDefinition(String fieldName) {
        return columnDefinitions.get(fieldName);
    }

    /*
    Data Management
     */

    public MappedSqlModel<T> mapObject(T valueObject) {
        return new MappedSqlModel<>(session, this, valueObject);
    }
}
