package net.mcmerdith.ormmicro.modeling;

public class ForeignObject<T> {
    public final ColumnDefinition foreignDefinition;
    public final T foreignObject;

    public ForeignObject(T foreignObject, ColumnDefinition foreignDefinition) {
        this.foreignObject = foreignObject;
        this.foreignDefinition = foreignDefinition;
    }

    public Object getReferenceValue() {
        return foreignDefinition.getFieldValue(foreignObject);
    }
}
