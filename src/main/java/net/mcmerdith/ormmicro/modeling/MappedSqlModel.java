package net.mcmerdith.ormmicro.modeling;

import net.mcmerdith.ormmicro.OrmMicroLogger;
import net.mcmerdith.ormmicro.internal.SessionFactory;
import net.mcmerdith.ormmicro.exceptions.SqlConstraintViolation;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MappedSqlModel<T> {
    private final SqlModel<T> model;
    private final T object;
    private final Map<String, Object> columns = new LinkedHashMap<>();

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

                List<?> nonNullValues;

                /*
                Pre-processing
                 */

                if (column.isForeignKey()) {
                    // Foreign keys are either defined or inferred
                    if (column.getForeignReferencedColumnDefinition() == null) {
                        // User manually defined the foreign reference
                        nonNullValues = valueStream.collect(Collectors.toList());
                    } else {
                        // Inferred FK references will be determined later
                        nonNullValues = valueStream.map(v ->
                                new ForeignObject<>(v, column.getForeignReferencedColumnDefinition())
                        ).collect(Collectors.toList());
                    }
                } else {
                    // Non-foreign keys must be converted first
                    nonNullValues = valueStream.map(v -> {
                        Object converted = column.convertJavaToSql(v);
                        if (converted == null) {
                            OrmMicroLogger.MODEL_MAPPER.error("Conversion error on field " + field);
                            return null;
                        }
                        return converted;
                    }).filter(Objects::nonNull).collect(Collectors.toList());
                }

                // If NOT NULl and value there are no non-null values, the NOT NULL constraint has been violated
                if (!column.isNullable() && nonNullValues.isEmpty())
                    throw new SqlConstraintViolation("Column `%s` was declared NOT NULL and `null` was provided");

                if (column.isArray() || column.isCollection()) {
                    // Arrays and collections are stored in an associated table

                    // The class is built based off the type we're using,
                    // we just don't know what type that is
                    //noinspection rawtypes
                    ElementCollectionTable cTable = column.getCollectionTable();
                    //noinspection unchecked
                    cTable.merge(nonNullValues);

                    columns.put(field, cTable);
                } else {
                    // Find the single element, or null if there isn't one
                    columns.put(field, nonNullValues.stream().findFirst().orElse(null));
                }
            } catch (Exception e) {
                OrmMicroLogger.MODEL_MAPPER.exception(e,
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
     * Get a map of column values, mapped by their field name on the model
     * <p>Returned object may be an {@link ElementCollectionTable}, containing the columns values</p>
     * <p>Column values may be a {@link ForeignObject}</p>
     *
     * @return A map of fieldName -> column value
     */
    public Map<String, Object> getColumns() {
        return new LinkedHashMap<>(columns);
    }

    public SqlModel<T> getModel() {
        return model;
    }

    public T getObject() {
        return object;
    }

    @Override
    public int hashCode() {
        ColumnDefinition id = model.getUniqueIdentifier();
        if (id != null) {
            Object idValue = id.getFieldValue(object);
            if (idValue != null) return Objects.hash(model.getTableName(), id.getName(), idValue);
        }

        return super.hashCode();
    }

    /**
     * Check if an object represents the same SqlModel
     * <p>Returns false if the object is not a MappedSqlModel, OR they do not map the same model</p>
     * <p>If both have an ID column, equality is determined by the value of the ID column,
     * otherwise equality is determined by if all fields match</p>
     * @return true if the object represents the same SqlModel as this model in the database
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MappedSqlModel)) return false;

        // If they are explicitly equal, return true
        if (super.equals(obj)) return true;

        MappedSqlModel<?> otherModel = (MappedSqlModel<?>) obj;

        // Table names must match to be equal
        if (!model.getTableName().equals(otherModel.model.getTableName())) return false;

        ColumnDefinition id = model.getUniqueIdentifier();
        ColumnDefinition otherId = otherModel.model.getUniqueIdentifier();

        // Both must either have matching IDs or not have an ID to be equal
        if (id == null || otherId == null) {
            // If either doesn't have an ID they are not equal
            return false;
        } else {
            // Both have ID

            // Models are not the same if their ID column is not the same
            if (!id.getName().equals(otherId.getName())) return false;

            Object idValue = id.getFieldValue(object);
            Object otherIdValue = otherId.getFieldValue(otherModel.object);

            // Models are equal if both have the same ID value
            return idValue.equals(otherIdValue);
        }
    }
}
