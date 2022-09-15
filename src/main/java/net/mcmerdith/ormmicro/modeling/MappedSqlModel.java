package net.mcmerdith.ormmicro.modeling;

import net.mcmerdith.ormmicro.OrmMicroLogger;
import net.mcmerdith.ormmicro.SessionFactory;
import net.mcmerdith.ormmicro.exceptions.SqlConstraintViolation;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MappedSqlModel<T> {
    private final SqlModel<T> model;
    private final T object;
    private final Map<String, Object> columns = new LinkedHashMap<>();
    private final Map<String, List<Object>> foreignObjects = new HashMap<>();

    public MappedSqlModel(SessionFactory session, SqlModel<T> model, T object) {
        this.model = model;
        this.object = object;

        model.getColumnDefinitions().forEach((field, column) -> {
            try {
                // Move any values from this column into a list for processing
                List<Object> values = new ArrayList<>();

                if (column.isArray()) {
                    Object[] fieldArrayValues = (Object[]) column.getFieldValue(object);
                    values.addAll(Arrays.asList(fieldArrayValues));
                } else if (column.isCollection()) {
                    values.addAll((Collection<?>) column.getFieldValue(object));
                } else {
                    values.add(column.getFieldValue(object));
                }

                // Stream the non-null values
                Stream<?> valueStream = values.stream().filter(Objects::nonNull);

                if (column.isForeignKey()) {
                    ColumnDefinition foreignDefinition = column.getForeignReferencedColumnDefinition();

                    if (foreignDefinition != null) {
                        // Column is being autodetected
                        // Map each object to the value of its primary key
                        List<Object> fObjs = new ArrayList<>();
                        valueStream = valueStream.map(fObj -> {
                            fObjs.add(fObj);
                            return foreignDefinition.getFieldValue(fObj);
                        });
                        foreignObjects.put(field, fObjs);
                    }
                } else {
                    valueStream = valueStream.map(column::convertJavaToSql);
                }

                List<?> preparedValues = valueStream.filter(Objects::nonNull).collect(Collectors.toList());

                // If NOT NULl and value is an empty list or null, the NOT NULL constraint has been violated
                if (!column.isNullable() && (preparedValues.isEmpty() || preparedValues.contains(null)))
                    throw new SqlConstraintViolation("Column `%s` was declared NOT NULL and `null` was provided");

                if (column.isArray() || column.isCollection()) {
                    // The class is built based off the type we're using,
                    // we just don't know what type that is

                    //noinspection rawtypes
                    ElementCollectionTable cTable = column.getCollectionTable();
                    //noinspection unchecked
                    cTable.merge(preparedValues);

                    columns.put(field, cTable);
                } else {
                    columns.put(field, preparedValues.stream().findFirst().orElse(null));
                }
            } catch (Exception e) {
                OrmMicroLogger.instance().exception(e,
                        String.format("Model for table `%s` mapped field '%s' -> column `%s` which does not exist on the mapped class '%s'",
                                model.getTableName(),
                                field,
                                column.getName(),
                                object.getClass().getSimpleName()
                        )
                );

                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Get a map of models associated with this object, mapped by their field name on the model
     * @return A map of fieldName -> Associated Object
     */
    public Map<String, Object> getForeignObjects() {
        return new HashMap<>(foreignObjects);
    }

    /**
     * Get a map of column values, mapped by their field name on the model
     * <p>Returned object may be an instance of {@link ElementCollectionTable}, signifying that the values should be stored in an associated table</p>
     * @return A map of fieldName -> column value
     */
    public Map<String, Object> getColumns() {
        return new LinkedHashMap<>(columns);
    }

    public SqlModel<T> getModel() {
        return model;
    }
}
