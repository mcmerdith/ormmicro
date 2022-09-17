package net.mcmerdith.ormmicro.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QueryResult<T> {
    private final Class<T> modelClass;
    private final List<T> results = new ArrayList<>();

    public QueryResult(Class<T> modelClass, Collection<T> results) {
        this.modelClass = modelClass;
        this.results.addAll(results);
    }

}
