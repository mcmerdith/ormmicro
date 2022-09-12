package net.mcmerdith.ormmicro.typing;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public interface AttributeConverter<J, S> {
    S convertToDatabaseColumn(J javaObject);

    J convertToModelAttribute(S databaseObject);

    /**
     * Get the types that an SSAttributeConverter is converting between
     *
     * @param converter
     * @return
     */
    @SuppressWarnings("rawtypes") // It doesn't matter what the conversion types are since we're just checking the names
    static Type[] getConversionTypes(Class<? extends AttributeConverter> converter) {
        // Determine the conversion types
        Type[] superTypes = converter.getGenericInterfaces();

        Type fromType = null;
        Type toType = null;

        for (Type superType : superTypes) {
            if (!(superType instanceof ParameterizedType)) continue;

            try {
                ParameterizedType generic = (ParameterizedType) superType;

                // We're looking for an SSAttributeConverter
                if (!generic.getRawType().getTypeName().equals(AttributeConverter.class.getTypeName()))
                    continue;

                Type[] types = generic.getActualTypeArguments();
                fromType = types[0];
                toType = types[1];
                break;
            } catch (Exception ignored) {
            }
        }

        return new Type[]{fromType, toType};
    }
}
