package net.mcmerdith.ormmicro.internal;

public interface NamingStrategy {
    String applyForColumn(String columnName);

    String applyForTable(String tableName);
}
