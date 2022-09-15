package net.mcmerdith.ormmicro;

import net.mcmerdith.ormmicro.modeling.MappedSqlModel;

import java.util.HashMap;
import java.util.Map;

/**
 * Maintain a register of objects currently tracked by the database
 */
public class PersistenceContext {
    private Map<Integer, MappedSqlModel<?>> tracked = new HashMap<>();

    public boolean isTracking(MappedSqlModel<?> o) {

    }

    public void setTracked(MappedSqlModel<?> o) {

    }

    public void setNonTracked(MappedSqlModel<?> o) {

    }

    public <T> MappedSqlModel<T> getTrackedInstance(T object) {

    }
}
