package net.mcmerdith.ormmicro;

import net.mcmerdith.ormmicro.modeling.MappedSqlModel;
import net.mcmerdith.ormmicro.modeling.SqlModel;
import net.mcmerdith.ormmicro.typing.SqlDialect;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ModelManager {
    private final SessionFactory session;

    protected ModelManager(SessionFactory session) {
        // Don't initialize this class
        this(session, SqlDialect.GENERIC);
    }

    protected ModelManager(SessionFactory session, SqlDialect dialect) {
        this.session = session;
        this.dialect = dialect;
    }

    private SqlDialect dialect;

    private final Map<Class<?>, SqlModel<?>> modelMappings = new ConcurrentHashMap<>();

    public void setDialect(SqlDialect dialect) {
        // Don't change the dialect unnecessarily
        if (this.dialect == dialect) return;

        this.dialect = dialect;

        // We need to rebuild the model mappings after changing dialects
        modelMappings.clear();
    }

    public SqlDialect getDialect() {
        return dialect;
    }

    /**
     * Register a model class with this manager
     * Class and declared fields will be traversed for annotations from the
     * {@link net.mcmerdith.ormmicro.annotations} package
     *
     * @param model The model class to be registered
     */
    public void registerModel(Class<?> model) {
        modelMappings.put(model, new SqlModel<>(session, model));
    }

    /**
     * Get an {@link SqlModel} associated with this class
     *
     * @param model The class to get the model of
     * @param <T>   The class that the model will be made more
     * @return An {@link SqlModel} of the class
     */
    @SuppressWarnings("unchecked")
    public <T> SqlModel<T> getModel(Class<T> model) {
        if (!modelMappings.containsKey(model)) registerModel(model);
        return (SqlModel<T>) modelMappings.get(model);
    }

    @SuppressWarnings("unchecked")
    public <T> MappedSqlModel<T> mapObject(T object) {
        SqlModel<T> model = getModel((Class<T>) object.getClass());
        return model.mapObject(object);
    }
}
