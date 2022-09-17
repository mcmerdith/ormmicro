package net.mcmerdith.ormmicro;

import net.mcmerdith.ormmicro.modeling.MappedSqlModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Maintain a register of objects currently tracked by the database
 */
public class PersistenceContext {
    private List<MappedSqlModel<?>> tracked = new ArrayList<>();

    /**
     * Check if a model is already in the database
     * @param o The model
     * @return If there is a MappedSqlModel representing the specific instance of the argument
     */
    public boolean isTracking(MappedSqlModel<?> o) {
        return tracked.contains(o);
    }

    public void track(MappedSqlModel<?> o) {
        // Make sure we're not tracking another instance of this model
        if (isTracking(o)) untrack(o);

        tracked.add(o);
    }

    public void untrack(MappedSqlModel<?> o) {
        tracked.remove(o);
    }
}
