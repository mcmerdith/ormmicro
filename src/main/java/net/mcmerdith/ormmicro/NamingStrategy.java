package net.mcmerdith.ormmicro;

public interface NamingStrategy {
    String applyForColumn(String columnName);

    String applyForTable(String tableName);
}
