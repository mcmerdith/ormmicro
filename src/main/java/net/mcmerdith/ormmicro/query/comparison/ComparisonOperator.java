package net.mcmerdith.ormmicro.query.comparison;

public enum ComparisonOperator {
    IS_NULL("IS NULL"),
    NOT_NULL("IS NOT NULL"),
    EQUAL("="),
    GREATER(">"),
    LESS("<"),
    GREATER_EQUAL(">="),
    LESS_EQUAL("<="),
    NOT_EQUAL("<>"),
    BETWEEN,
    LIKE,
    IN;

    private final String sql;

    public String getSql() {
        return this.sql;
    }

    ComparisonOperator() {
        this.sql = this.name();
    }

    ComparisonOperator(String sql) {
        this.sql = sql;
    }
}
