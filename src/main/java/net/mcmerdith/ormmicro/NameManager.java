package net.mcmerdith.ormmicro;

public class NameManager {
    private static final NamingStrategy NO_OP = new NamingStrategy() {
        @Override
        public String applyForColumn(String columnName) {
            return columnName;
        }

        @Override
        public String applyForTable(String tableName) {
            return tableName;
        }
    };

    protected NameManager() {
        this(NO_OP);
    }

    protected NameManager(NamingStrategy strategy) {
        this.currentStrategy = strategy;
    }

    /*
    Apply a mapping for column and table names
     */

    private NamingStrategy currentStrategy;

    public String applyStrategiesForColumn(String columnName) {
        return currentStrategy.applyForColumn(columnName);
    }

    public String applyStrategiesForTable(String tableName) {
        return currentStrategy.applyForTable(tableName);
    }

    public void setStrategy(NamingStrategy strategy) {
        if (strategy == null) {
            currentStrategy = NO_OP;
        } else {
            currentStrategy = strategy;
        }
    }
}
