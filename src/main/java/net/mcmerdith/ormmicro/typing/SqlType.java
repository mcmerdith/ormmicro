package net.mcmerdith.ormmicro.typing;

public enum SqlType {
    INTEGER(null, SQLite.INTEGER, true),
    FLOAT(null, SQLite.REAL, false),
    DOUBLE(null, SQLite.REAL, false),
    DECIMAL(null, SQLite.REAL, false),
    /**
     * SQL Type: `VARCHAR`
     */
    STRING("VARCHAR", SQLite.TEXT, false),
    BOOLEAN(null, SQLite.NUMERIC, false),
    TEXT(null, SQLite.TEXT, true),
    BLOB(null, SQLite.BLOB, true),
    DATE(null, SQLite.REAL, false),
    DATETIME(null, SQLite.REAL, false),
    TIMESTAMP(null, SQLite.REAL, false),
    /**
     * Auto-detect SQL DDL from field type
     */
    AUTO(null, null, false);

    private final String value;
    private final SQLite sqlite;
    /**
     * This is not the same as `length`<br>
     * Sizeable indicates if the type accepts a prefix: (TINY/MEDIUM/LONG)
     */
    private final boolean sizeable;

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

    SqlType(String value, SQLite sqlite, boolean sizeable) {
        this.value = value;
        this.sqlite = sqlite;
        this.sizeable = sizeable;
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
