package net.mcmerdith.ormmicro.modeling;

public class SqlConstraint {
    public enum Type {
        UNIQUE("UK"),
        PRIMARY_KEY("PK"),
        FOREIGN("FK"),
        CHECK("CHK");

        private final String idPrefix;

        public String getIdPrefix() {
            return this.idPrefix;
        }

        public String getSql() {
            return this.name().replace("_", " ");
        }

        Type(String idPrefix) {
            this.idPrefix = idPrefix;
        }
    }

    private final String id;
    private final Type type;
    private final String definition;

    public SqlConstraint(String id, Type type, String definition) {
        this.id = id;
        this.type = type;
        this.definition = definition;
    }

    public String buildSql() {
        return this.buildSql(true);
    }

    public String buildSql(boolean includeDefinition) {
        StringBuilder sql = new StringBuilder();

        // Add the constraint id
        sql.append("CONSTRAINT ").append(this.type.getIdPrefix()).append("_").append(this.id);

        // TODO different result based on dialect
        // If we don't need the definition return;
        if (!includeDefinition) return sql.toString();

        // Add the constraint definition
        sql.append(" ").append(this.type.getSql()).append(" ").append(this.definition);

        return sql.toString();
    }
}
