package net.mcmerdith.ormmicro.query;

public enum SqlJoinDirection {
    INNER("INNER JOIN"),
    LEFT("LEFT JOIN"),
    RIGHT("RIGHT JOIN"),
    FULL("FULL JOIN");

    private final String sql;

    public String getSql() {
        return this.sql;
    }

    SqlJoinDirection(String sql) {
        this.sql = sql;
    }
}
