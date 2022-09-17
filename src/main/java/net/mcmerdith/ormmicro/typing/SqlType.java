package net.mcmerdith.ormmicro.typing;

import java.sql.Types;

public enum SqlType {
    INTEGER(null, SQLite.INTEGER, true, Types.INTEGER),
    FLOAT(null, SQLite.REAL, false, Types.FLOAT),
    DOUBLE(null, SQLite.REAL, false, Types.DOUBLE),
    DECIMAL(null, SQLite.REAL, false, Types.DECIMAL),
    /**
     * SQL Type: `VARCHAR`
     */
    STRING("VARCHAR", SQLite.TEXT, false, Types.VARCHAR),
    BOOLEAN(null, SQLite.NUMERIC, false, Types.BOOLEAN),
    TEXT(null, SQLite.TEXT, true, Types.LONGVARCHAR),
    BLOB(null, SQLite.BLOB, true, Types.BLOB),
    DATE(null, SQLite.REAL, false, Types.DATE),
    DATETIME(null, SQLite.REAL, false, Types.DATE),
    TIMESTAMP(null, SQLite.REAL, false, Types.TIMESTAMP),
    /**
     * Auto-detect SQL DDL from field type
     */
    AUTO(null, null, false, Integer.MIN_VALUE);

    private final String value;
    private final SQLite sqlite;
    /**
     * This is not the same as `length`<br>
     * Sizeable indicates if the type accepts a prefix: (TINY/MEDIUM/LONG)
     */
    private final boolean sizeable;

    private final int jdbcTypeCode;

    public boolean isIncrementable() {
        return this == INTEGER || this == FLOAT || this == DOUBLE;
    }

    public String getSqlValue() {
        return getSqlValue(null);
    }

    public String getSqlValue(Size size) {
        String base = (this.value == null) ? this.name() : this.value;
        if (!this.sizeable || size == null || size == Size.NONE) return base;
        return String.format("%s%s", size.name(), base);
    }

    public String getSqliteValue() {
        return this.sqlite.name();
    }

    public SQLite getSqliteType() {
        return this.sqlite;
    }

    public int getJdbcTypeCode() { return this.jdbcTypeCode; }

    SqlType(String value, SQLite sqlite, boolean sizeable, int jdbcTypeCode) {
        this.value = value;
        this.sqlite = sqlite;
        this.sizeable = sizeable;
        this.jdbcTypeCode = jdbcTypeCode;
    }

    /**
     * Define the available SQLite types
     */
    private enum SQLite {
        INTEGER,
        TEXT,
        BLOB,
        REAL,
        NUMERIC
    }

    public enum Size {
        TINY,
        MEDIUM,
        BIG,
        LONG,
        NONE
    }
}
