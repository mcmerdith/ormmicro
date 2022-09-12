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
    private final Map<String, Object> columns = new LinkedHashMap<>();
    private final List<ElementCollectionTable<?>> collections = new ArrayList<>();
    private final List<Object> foreignObjects = new ArrayList<>();

    public MappedSqlModel(SessionFactory session, SqlModel<T> model, T object) {
        this.model = model;

        model.getColumnDefinitions().forEach((field, column) -> {
            try {
                String columnName = column.getName();

                Field actualField = object.getClass().getDeclaredField(field);
                // who cares what the intended visibility is anyway
                actualField.setAccessible(true);

                // Move any values from this column into a list for processing
                List<Object> values = new ArrayList<>();

                if (column.isArray()) {
                    Object[] fieldArrayValues = (Object[]) actualField.get(object);
                    values.addAll(Arrays.asList(fieldArrayValues));
                } else if (column.isCollection()) {
                    values.addAll((Collection<?>) actualField.get(object));
                } else {
                    values.add(actualField.get(object));
                }

                // Stream the non-null values
                Stream<?> valueStream = values.stream().filter(Objects::nonNull);

                if (column.isForeignKey()) {
                    ColumnDefinition foreignDefinition = column.getForeignReferencedColumnDefinition();

                    if (foreignDefinition != null) {
                        // Column is being autodetected
                        // Map each object to the value of its primary key
                        valueStream = valueStream.map(fObj -> {
                            Object reference = extractForeignKeyReference(fObj, foreignDefinition.getField().getName());

                            if (reference != null) {
                                foreignObjects.add(fObj);
                            }

                            return reference;
                        });
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

                    collections.add(cTable);
                } else {
                    columns.put(columnName, preparedValues.stream().findFirst().orElse(null));
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

    private Object extractForeignKeyReference(Object object, String referenceField) {
        try {
            // Get the referenced field on the foreign object
            Field foreignField = object.getClass().getDeclaredField(referenceField);
            foreignField.setAccessible(true);
            return foreignField.get(object);
        } catch (Exception e) {
            OrmMicroLogger.instance().exception(e,
                    String.format("Failed to get foreign reference `%s` on type '%s'",
                            referenceField,
                            object.getClass().getSimpleName()));
            return null;
        }
    }

    public List<Object> getForeignObjects() {
        return new ArrayList<>(foreignObjects);
    }

    public List<ElementCollectionTable<?>> getCollections() {
        return new ArrayList<>(collections);
    }

    public Map<String, Object> getColumns() {
        return new LinkedHashMap<>(columns);
    }

    public SqlModel<T> getModel() {
        return model;
    }
}
