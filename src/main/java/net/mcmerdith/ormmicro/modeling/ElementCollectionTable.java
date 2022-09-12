package net.mcmerdith.ormmicro.modeling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ElementCollectionTable<T> {
    public final String name;
    public final String referenceColumnName;
    public final String valueColumnName;

    private final List<T> elements = new ArrayList<>();

    public ElementCollectionTable(Class<T> elementType, String name, String referenceColumnName, String valueColumnName) {
        this(name, referenceColumnName, valueColumnName);
    }

    public ElementCollectionTable(String name, String referenceColumnName, String valueColumnName) {
        this.name = name;
        this.referenceColumnName = referenceColumnName;
        this.valueColumnName = valueColumnName;
    }

    public void merge(Collection<T> values) {
        elements.addAll(values);
    }

    public void merge(T[] values) {
        elements.addAll(Arrays.asList(values));
    }

    public List<T> getElements() {
        return new ArrayList<>(elements);
    }
}
