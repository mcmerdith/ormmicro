package net.mcmerdith.ormmicro.internal;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.mcmerdith.ormmicro.OrmMicroLogger;
import net.mcmerdith.ormmicro.typing.GenericTypeMapper;
import net.mcmerdith.ormmicro.typing.ISqlTypeMapper;
import net.mcmerdith.ormmicro.typing.SqlDialect;

import javax.annotation.Nonnull;
import java.util.Properties;
import java.util.Timer;
import java.util.logging.Level;

public class SessionFactory implements AutoCloseable {
    /*
    Managers
     */

    private final NameManager nameManager;
    private final ModelManager modelManager;
    private final ISqlTypeMapper typeMapper;

    public NameManager getNameManager() {
        return nameManager;
    }

    public ModelManager getModelManager() {
        return modelManager;
    }

    public ISqlTypeMapper getTypeMapper() {
        return typeMapper;
    }

    /*
    Database
     */

    private final HikariDataSource dataSource;

    protected HikariDataSource getDataSource() {
        return dataSource;
    }

    private final PersistenceContext persistenceContext = new PersistenceContext();

    public PersistenceContext getPersistenceContext() {
        return persistenceContext;
    }

    private final Timer workerThread = new Timer();
    public final DatabaseWorker worker;

    /**
     * Get the Database worker
     */
    public DatabaseWorker getWorker() {
        return worker;
    }

    private SessionFactory(NameManager nameManager, SqlDialect dialect, ISqlTypeMapper typeMapper, HikariDataSource dataSource) {
        this.nameManager = nameManager;
        this.modelManager = new ModelManager(this, dialect);
        this.typeMapper = typeMapper;
        this.dataSource = dataSource;
        this.worker = new DatabaseWorker(this);
        workerThread.scheduleAtFixedRate(this.worker, 2000, 250);
    }

    public void setLogLevel(Level level) {
        try {
            OrmMicroLogger.setLogLevels(level);
            dataSource.getParentLogger().setLevel(level);
        } catch (Exception ignored) {
        }
    }

    public Session getCurrentSession() {
        return new Session(this);
    }

    @Override
    public void close() {
        worker.cancel();
        workerThread.cancel();
    }

    public static class Builder {
        private NameManager nameManager;
        private SqlDialect sqlDialect;
        private ISqlTypeMapper typeMapper;
        private HikariConfig hikariConfig;

        public Builder(@Nonnull HikariConfig hikariConfig) {
            this.hikariConfig = hikariConfig;
        }

        public void setConfiguration(HikariConfig hikariConfig) {
            if (hikariConfig != null) this.hikariConfig = hikariConfig;
        }

        public void addDataSourceProperties(Properties properties) {
            hikariConfig.setDataSourceProperties(properties);
        }

        public void setNamingStrategy(NamingStrategy strategy) {
            if (nameManager == null) {
                nameManager = new NameManager(strategy);
                return;
            }

            nameManager.setStrategy(strategy);
        }

        public void setDialect(SqlDialect dialect) {
            this.sqlDialect = dialect;
        }

        public void setTypeMapper(ISqlTypeMapper typeMapper) {
            this.typeMapper = typeMapper;
        }

        public SessionFactory build() {
            if (nameManager == null) nameManager = new NameManager();
            if (sqlDialect == null) sqlDialect = SqlDialect.GENERIC;
            if (typeMapper == null) typeMapper = new GenericTypeMapper();
            return new SessionFactory(nameManager, sqlDialect, typeMapper, new HikariDataSource(hikariConfig));
        }
    }
}
