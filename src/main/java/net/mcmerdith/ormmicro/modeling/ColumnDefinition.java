package net.mcmerdith.ormmicro.modeling;

import net.mcmerdith.ormmicro.OrmMicroLogger;
import net.mcmerdith.ormmicro.SessionFactory;
import net.mcmerdith.ormmicro.annotations.*;
import net.mcmerdith.ormmicro.typing.AttributeConverter;
import net.mcmerdith.ormmicro.typing.ColumnType;
import net.mcmerdith.ormmicro.typing.SqlType;
import net.mcmerdith.ormmicro.util.StringUtils;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class ColumnDefinition {
    /*
    Column Definition
     */

    private String columnName = null;

    private ColumnType annotationColumnType = null;
    private String customColumnDefinition = "";

    private String defaultColumnValue = "";

    /*
    Typing
     */

    // Implicit annotatedField.getType();
    private Class<?> convertedType = null;
    private Class<?> collectionType = null;

    /**
     * Get the final type of this field, after collection unwrapping (if needed) and type conversion (if needed)
     * and foreign key mapping
     *
     * @return The final type of this field that will be mapped to the database
     */
    public Class<?> getFieldType() {
        if (foreignReferencedColumnDefinition != null) return foreignReferencedColumnDefinition.getFieldType();
        if (convertedType != null) return convertedType;
        if (collectionType != null) return collectionType;
        return annotatedField.getType();
    }

    /**
     * Get the final column type.
     *
     * @return The defined type, foreign column type if present, else the type implied by the field
     * ({@link ColumnDefinition#getFieldType()}), else null
     */
    public ColumnType getColumnType() {
        if (annotationColumnType != null && annotationColumnType.type != SqlType.AUTO) return annotationColumnType;
        if (foreignReferencedColumnDefinition != null) return foreignReferencedColumnDefinition.getColumnType();
        if (getFieldType().isEnum()) return enumStorageMode.getType();
        return session.getTypeMapper().javaToSqlType(getFieldType());
    }

    /*
    Constraints
     */

    private boolean unique = false;

    private boolean primary = false;
    private boolean autoIncrement = false;

    private boolean nullable = true;

    private boolean foreign = false;
    private String foreignReference = null;
    private ColumnDefinition foreignReferencedColumnDefinition = null;

    private String checkConstraint = null;

    /*
    Properties
     */

    private final boolean isTransient;

    private EnumStorage.Mode enumStorageMode = EnumStorage.Mode.VALUE;

    /*
    Arrays, Collections
     */

    private boolean array = false;

    private boolean collection = false;

    private Supplier<ElementCollectionTable<?>> collectionTable = null;

    /*
    Data converters
     */

    private final List<AttributeConverter<?, ?>> converters = new ArrayList<>();

    /*
    Objects used to build this column
     */

    private final SessionFactory session;
    private final SqlModel<?> model;
    private final Field annotatedField;

    public ColumnDefinition(SessionFactory session, SqlModel<?> model, Field annotatedField) {
        this.session = session;
        this.model = model;
        this.annotatedField = annotatedField;

        Transient transientAnnotation = annotatedField.getDeclaredAnnotation(Transient.class);

        // Transient fields are not tracked by the database
        isTransient = transientAnnotation != null;
        if (isTransient) return;

        this.annotatedField.setAccessible(true);

        Column column = annotatedField.getDeclaredAnnotation(Column.class);

        if (column == null || StringUtils.isBlank(column.name())) {
            columnName = annotatedField.getName().toLowerCase();
        } else {
            columnName = column.name();
        }

        // Check if the field is numerous, if so, extract the type
        processElementCollectionAnnotation(annotatedField.getDeclaredAnnotation(ElementCollection.class));

        // Load any converters, they'll be needed to determine the datatype
        processConversions(annotatedField);

        // Determine the defined properties of the column
        // Name, Defined type, Custom definition, Nullable, Unique
        processColumnAnnotation(column);

        // Check if it's a primary key
        processIdAnnotation(annotatedField.getDeclaredAnnotation(Id.class));

        // Check if it's a foreign key
        processForeignKeyAnnotation(annotatedField.getDeclaredAnnotation(ForeignKey.class));

        /*
        Properties
         */

        // Get the enum storage mode
        processEnumTypeAnnotation(annotatedField.getDeclaredAnnotation(EnumStorage.class));
    }

    /*
    Util Functions
     */

    /**
     * If field is annotated with {@link Convert} load the converters<br>
     * Trace the converters from start to finish linking their conversion types together as:
     * `implicit type -> c1 -> c2 ->  ... -> final type`
     * <p>Method will fail if the converter takes a different type than the previous converter returns (or the implicit type)</p>
     */
    private void processConversions(Field field) {
        Converts converts = field.getDeclaredAnnotation(Converts.class);

        List<Class<? extends AttributeConverter<?, ?>>> converters = new ArrayList<>();

        if (converts == null) {
            Convert convert = field.getDeclaredAnnotation(Convert.class);

            if (convert == null) return;

            converters.add(convert.converter());
        } else {
            for (Convert convert : converts.value()) {
                converters.add(convert.converter());
            }
        }

        processConverters(converters);
    }

    private void processConverters(List<Class<? extends AttributeConverter<?, ?>>> converters) {
        // Load converters
        if (converters == null || converters.isEmpty()) return;

        Type currentType = getFieldType();

        for (Class<? extends AttributeConverter<?, ?>> converterClass : converters) {
            Type[] generics = AttributeConverter.getConversionTypes(converterClass);

            if (generics[0] == null || generics[1] == null) {
                annotationError("Convert", "could not determine types to convert");

                converters.clear();

                return;
            }

            Type fromType = generics[0];
            Type toType = generics[1];
            if (fromType.equals(currentType)) {
                currentType = toType;
            } else {
                annotationError("Convert", String.format("`%s` -> `%s` is not applicable for type `%s`%s",
                        fromType.getTypeName(),
                        toType.getTypeName(),
                        currentType.getTypeName(),
                        converters.size() > 1 ? " (are they in order?)" : ""));

                converters.clear();

                return;
            }

            try {
                Constructor<? extends AttributeConverter<?, ?>> constructor = converterClass.getConstructor();
                constructor.setAccessible(true);
                this.converters.add(constructor.newInstance());
            } catch (Exception e) {
                // Log the error
                annotationError("Convert", String.format("was unable to instantiate converter for `%s` -> `%s`",
                        fromType,
                        toType));

                converters.clear();

                return;
            }
        }

        try {
            String tName;

            if (currentType instanceof ParameterizedType) {
                tName = ((ParameterizedType) currentType).getRawType().getTypeName();
            } else {
                tName = currentType.getTypeName();
            }

            convertedType = Class.forName(tName);
        } catch (Exception e) {
            annotationError("Convert", String.format("encountered reflection error determining final type '%s'", currentType.getTypeName()));
        }
    }

    private void processElementCollectionAnnotation(@Nullable ElementCollection elementCollection) {
        if (elementCollection == null) return;

        array = annotatedField.getType().isArray();
        collection = Collection.class.isAssignableFrom(getFieldType());

        if (!array && !collection) {
            annotationError(elementCollection, "can only be applied to Array or Collection fields");
            return;
        }

        if (array) {
            collectionType = getFieldType().getComponentType();
        }

        if (collection) {
            Type fType = annotatedField.getGenericType();

            if (fType instanceof ParameterizedType) {
                try {
                    Type[] fGenerics = ((ParameterizedType) fType).getActualTypeArguments();
                    collectionType = Class.forName(fGenerics[0].getTypeName());
                } catch (Exception e) {
                    annotationError(elementCollection, "could not load type: " + e.getMessage());

                    array = false;
                    collection = false;

                    return;
                }
            } else {
                annotationError(elementCollection, "could not determine type: raw use of generic Collection");

                array = false;
                collection = false;

                return;
            }
        }

        String cTableName = elementCollection.associatedTableName();
        String cRefColName = elementCollection.referenceColumnName();
        String cValColName = elementCollection.valueColumnName();

        if (StringUtils.isBlank(cTableName)) {
            cTableName = columnName;
        }

        if (StringUtils.isBlank(cRefColName)) {
            cRefColName = model.getTableName(true) + "_id";
        }

        if (StringUtils.isBlank(cValColName)) {
            cValColName = columnName;
        }

        // Create the supplier

        final String cTN = model.getTableName(true) + "_" + cTableName;
        final String cRCN = cRefColName;
        final String cVCN = cValColName;

        this.collectionTable = () -> new ElementCollectionTable<>(getFieldType(), cTN, cRCN, cVCN);
    }

    private void processColumnAnnotation(@Nullable Column column) {
        if (column == null) return;

        annotationColumnType = new ColumnType.Builder(column.definition())
                .setSize(column.size())
                .setLength(column.length())
                .setPrecision(column.precision())
                .setDigits(column.digits())
                .setDecimals(column.decimals())
                .build();
        nullable = column.nullable();
        unique = column.unique();
        customColumnDefinition = column.customDefinition();
        checkConstraint = column.check();

        // Set the default value
        if (!StringUtils.isBlank(column.defaultValue())) {
            this.defaultColumnValue = column.defaultValue();
        } else if (this.nullable) {
            this.defaultColumnValue = "NULL";
        }
    }

    private void processIdAnnotation(@Nullable Id id) {
        if (id == null) return;

        primary = true;

        if (id.autoIncrement()) {
            // Make sure we can actually auto-increment it
            // If the user defined a type we can't validate it, so trust they know what they're doing
            if (!getColumnType().type.isIncrementable() && customColumnDefinition == null) {
                annotationError(id, "autoincrement can only be applied to numeric fields!");
                return;
            }

            autoIncrement = true;
        }
    }

    private void processForeignKeyAnnotation(@Nullable ForeignKey foreignKey) {
        if (foreignKey == null) return;

        String externalReference = foreignKey.externalReference();

        if (StringUtils.notBlank(externalReference)) {
            foreign = true;
            foreignReference = externalReference;
            return;
        }

        // User did not define a reference, so calculate one

        // Get the ID column on the referenced model
        ColumnDefinition foreignReferenceColumnID = session.getModelManager().getModel(getFieldType()).getUniqueIdentifier();

        if (foreignReferenceColumnID != null) {
            foreign = true;
            foreignReferencedColumnDefinition = foreignReferenceColumnID;
            return;
        }

        // If the user didn't define an association column
        // and the model doesn't have an id (primary or unique key)
        // We cannot assume the column to reference by
        annotationError(foreignKey, "cannot assume reference column on a table with no primary or unique keys");
    }

    private void processEnumTypeAnnotation(@Nullable EnumStorage enumStorage) {
        if (enumStorage == null) return;

        this.enumStorageMode = enumStorage.mode();
    }

    private void annotationError(Annotation annotation, String error) {
        annotationError(annotation.annotationType().getSimpleName(), error);
    }

    private void annotationError(String annotation, String error) {
        OrmMicroLogger.instance().error(
                String.format(
                        "%s annotation cannot be applied to %s. %s %s",
                        annotation,
                        columnName == null ? String.format("field '%s'", annotatedField.getName())
                                : String.format("column `%s`", columnName),
                        annotation,
                        error)
        );
    }

    /*
     * Getters
     */

    /**
     * Get the name of this column
     */
    public String getName() {
        return getName(false);
    }

    protected String getName(boolean raw) {
        return raw ? columnName : session.getNameManager().applyStrategiesForTable(columnName);
    }

    /**
     * Get the SQL definition for this column
     */
    public String getDefinition() {
        return getDefinition(true);
    }

    public String getDefinition(boolean includeConstraints) {
        // TODO deal with
        StringBuilder definition = new StringBuilder();

        // Add the column name
        definition.append(getName()).append(" ");

        // Get this columns type definition
        if (StringUtils.isBlank(customColumnDefinition)) {
            definition.append(getColumnType().getDefinition(session.getModelManager().getDialect()));
        } else {
            definition.append(customColumnDefinition);
        }

        // If we don't need the constraints return
        if (!includeConstraints) return definition.toString();

        // Add the constraints
        if (!isNullable()) definition.append(" NOT NULL");
        if (autoIncrement) definition.append(" AUTO_INCREMENT");
        if (!StringUtils.isBlank(defaultColumnValue)) definition.append(" DEFAULT ").append(defaultColumnValue);

        return definition.toString();
    }

    public List<SqlConstraint> getConstraints() {
        List<SqlConstraint> constraints = new ArrayList<>();

        if (primary) constraints.add(
                new SqlConstraint(model.getTableName(true), SqlConstraint.Type.PRIMARY_KEY, "(" + getName() + ")")
        );

        if (unique) constraints.add(
                new SqlConstraint(getName(), SqlConstraint.Type.UNIQUE, "(" + getName() + ")")
        );

        if (foreign) constraints.add(
                new SqlConstraint(getName(), SqlConstraint.Type.FOREIGN, "(" + getName() + ") REFERENCES " + getForeignReference())
        );

        if (!StringUtils.isBlank(checkConstraint)) constraints.add(
                new SqlConstraint(getName(), SqlConstraint.Type.CHECK, "(" + checkConstraint + ")")
        );

        return Collections.unmodifiableList(constraints);
    }

    public boolean isPrimary() {
        return primary;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public boolean isNullable() {
        return nullable;
    }

    public boolean isUnique() {
        return unique;
    }

    public boolean isForeignKey() {
        return foreign;
    }

    public String getForeignReference() {
        if (StringUtils.notBlank(foreignReference)) return foreignReference;
        return String.format("%s(%s)",
                foreignReferencedColumnDefinition.getModel().getTableName(),
                foreignReferencedColumnDefinition.getName());
    }

    public ColumnDefinition getForeignReferencedColumnDefinition() {
        return foreignReferencedColumnDefinition;
    }

    public boolean isTransient() {
        return isTransient;
    }

    public EnumStorage.Mode getEnumStorageMode() {
        return enumStorageMode;
    }

    public boolean isArray() {
        return array;
    }

    public boolean isCollection() {
        return collection;
    }

    public ElementCollectionTable<?> getCollectionTable() {
        return collectionTable.get();
    }

    public Field getField() {
        return this.annotatedField;
    }

    public SqlModel<?> getModel() {
        return model;
    }

    public boolean needsConversion() {
        return !converters.isEmpty();
    }

    /*
    Data Conversion
     */

    public Object getFieldValue(Object o) {
        try {
            // Get the referenced field on the foreign object
            return annotatedField.get(o);
        } catch (Exception e) {
            OrmMicroLogger.instance().exception(e,
                    String.format("Failed to get field `%s` on type '%s'",
                            annotatedField.getName(),
                            o.getClass().getSimpleName()));
            return null;
        }
    }

    /**
     * Convert an object of this field's inherent type to this column's Sql type
     * using the Data Converters applied to this column
     *
     * @param o The object to convert
     * @return The object, if no converters, null if there was a conversion error,
     * else the object after being passed through the converter chain
     */
    public Object convertJavaToSql(Object o) {
        if (o == null) return null;

        Object convert = applyConverterChain(o, converters);

        if (convert == null) return null;

        if (convert.getClass().isEnum()) {
            Enum<?> enumValue = (Enum<?>) o;

            switch (getEnumStorageMode()) {
                case ORDINAL:
                    convert = enumValue.ordinal();
                    break;
                case VALUE:
                default:
                    convert = enumValue.name();
                    break;
            }
        }

        return convert;
    }

    /**
     * Convert an object of this column's Sql type to this field's inherent type
     * using the Data Converters applied to this column
     *
     * @param o The object to convert
     * @return The object, if no converters, null if there was a conversion error,
     * else the object after being passed through the converter chain
     */
    public Object convertSqlToJava(Object o) {
        if (converters.isEmpty() || o == null) return o;

        // The Java -> Sql converters, reversed
        List<AttributeConverter<?, ?>> sqlConverters = new ArrayList<>(converters);
        Collections.reverse(sqlConverters);

        // Run it through the converter chain
        Object result = applyConverterChain(o, sqlConverters);

        // If the converters returned nothing, return null and let the caller figure it out
        if (result == null) return null;

        // If it's not an enum no further conversion is necessary
        if (!getFieldType().isEnum()) return result;

        Object[] constants = getFieldType().getEnumConstants();
        switch (getEnumStorageMode()) {
            case ORDINAL:
                try {
                    return constants[Integer.parseInt(result.toString())];
                } catch (IndexOutOfBoundsException e) {
                    OrmMicroLogger.instance().error(
                            String.format("Enum '%s' does not contain ordinal %s",
                                    getFieldType().getTypeName(),
                                    result)
                    );
                    return null;
                } catch (NumberFormatException e) {
                    OrmMicroLogger.instance().error(
                            String.format("'%s' is not a valid ordinal constant for Enum '%s'. Matching by name()",
                                    result,
                                    getFieldType().getTypeName())
                    );
                }
            case VALUE:
            default:
                for (Object constant : constants) {
                    try {
                        Method nameMethod = constant.getClass().getMethod("name");
                        String name = (String) nameMethod.invoke(constant);
                        if (name.equals(result)) return constant;
                    } catch (NoSuchMethodException e) {
                        OrmMicroLogger.instance().error(
                                String.format("Enum constant for '%s' does not provide a name() method",
                                        constant.getClass().getTypeName())
                        );
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        OrmMicroLogger.instance().exception(e,
                                String.format("Error getting name of Enum '%s'",
                                        constant.getClass().getTypeName())
                        );
                    }
                }
                return null;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"}) // Generic types were checked when the converter list was built
    private Object applyConverterChain(Object o, List<AttributeConverter<?, ?>> converters) {
        Object current = o;

        for (AttributeConverter converter : converters) {
            try {
                current = converter.convertToDatabaseColumn(current);
            } catch (Exception e) {
                Type[] generics = AttributeConverter.getConversionTypes(converter.getClass());

                String t0 = generics[0] == null ? "Unknown" : generics[0].getTypeName();
                String t1 = generics[1] == null ? "Unknown" : generics[1].getTypeName();

                OrmMicroLogger.instance().exception(e, String.format("Failed to convert type '%s' using '%s' -> '%s'", o.getClass().getSimpleName(), t0, t1));
                return null;
            }
        }

        return current;
    }
}
