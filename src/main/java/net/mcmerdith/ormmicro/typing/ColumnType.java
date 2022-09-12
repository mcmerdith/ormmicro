package net.mcmerdith.ormmicro.typing;

import net.mcmerdith.ormmicro.OrmMicroLogger;

import javax.annotation.Nonnull;
import java.util.Objects;

public class ColumnType {
    public static ColumnType STRING = new ColumnType.Builder(SqlType.STRING).build();
    public static ColumnType TINYINT = new ColumnType.Builder(SqlType.INTEGER)
            .setSize(SqlType.Size.TINY)
            .build();
    public static ColumnType MEDIUMINT = new ColumnType.Builder(SqlType.INTEGER)
            .setSize(SqlType.Size.MEDIUM)
            .build();
    public static ColumnType INTEGER = new ColumnType.Builder(SqlType.INTEGER).build();
    public static ColumnType BIGINT = new ColumnType.Builder(SqlType.INTEGER)
            .setSize(SqlType.Size.BIG)
            .build();
    public static ColumnType FLOAT = new ColumnType.Builder(SqlType.FLOAT).build();
    public static ColumnType DOUBLE = new ColumnType.Builder(SqlType.DOUBLE).build();
    public static ColumnType BOOLEAN = new ColumnType.Builder(SqlType.BOOLEAN).build();

    public static final int DEFAULT_DIGITS = 10;
    public static final int DEFAULT_DECIMALS = 0;

    public final SqlType type;
    public final SqlType.Size size;
    public final Integer length;
    public final Integer precision;
    public final Integer digits;
    public final Integer decimals;

    private ColumnType(@Nonnull SqlType type, SqlType.Size size, Integer length, Integer precision, Integer digits, Integer decimals) {
        this.type = type;
        this.size = size;
        this.length = (length != null && length > 0) ? length : null;
        this.precision = (precision != null && precision > 0) ? precision : null;

        this.digits = digits;
        this.decimals = decimals;
    }

    public String getDefinition(SqlDialect dialect) {
        if (dialect == SqlDialect.SQLITE) {
            // Sqlite types have a predefined size, length, precision and scale
            return this.type.getSqliteValue();
        } else {
            StringBuilder definitionBuilder = new StringBuilder();

            // Start with the column type, with size if applicable
            definitionBuilder.append(this.type.getSqlValue(this.size));

            boolean sized = this.size != null && this.size != SqlType.Size.NONE;

            // Only INT accepts a length argument when a default size is specified
            if (sized && this.type != SqlType.INTEGER) return definitionBuilder.toString();

            // Apply the appropriate arguments for the type
            switch (this.type) {
                case DECIMAL:
                    definitionBuilder
                            .append("(")
                            .append(this.digits == null ? DEFAULT_DIGITS : this.digits)
                            .append(", ")
                            .append(this.decimals == null ? DEFAULT_DECIMALS : this.decimals)
                            .append(")");

                    break;
                case DOUBLE:
                case FLOAT:
                    if (this.precision != null)
                        definitionBuilder
                                .append("(")
                                .append(this.precision)
                                .append(")");

                    break;
                case STRING:
                case TEXT:
                case BLOB:
                case INTEGER:
                    definitionBuilder
                            .append("(");

                    if (this.length != null) {
                        definitionBuilder
                                .append(this.length);
                    } else {
                        int defaultLength = 255;

                        // TEXT and BLOB are biggg
                        if (type == SqlType.TEXT || type == SqlType.BLOB) defaultLength = 65535;

                        definitionBuilder
                                .append(defaultLength);
                    }

                    definitionBuilder
                            .append(")");

                    break;
                case BOOLEAN:
                case DATE:
                case DATETIME:
                case TIMESTAMP:
                case AUTO:
                default:
                    break;
            }

            return definitionBuilder.toString();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ColumnType)) return false;

        ColumnType otherType = (ColumnType) obj;

        if (!type.getSqlValue(size).equals(otherType.type.getSqlValue(otherType.size))) return false;

        switch (this.type) {
            case DECIMAL:
                return (this.digits == null && otherType.digits == null)
                        || (this.digits != null && this.digits.equals(otherType.digits))
                        || (this.decimals == null && otherType.decimals == null)
                        || (this.decimals != null && this.decimals.equals(otherType.decimals));
            case DOUBLE:
            case FLOAT:
                if (this.precision == null) return otherType.precision == null;
                return this.precision.equals(otherType.precision);
            case STRING:
            case TEXT:
            case BLOB:
            case INTEGER:
                if (this.length == null) return otherType.length == null;

                return this.length.equals(otherType.length);
            case BOOLEAN:
            case DATE:
            case DATETIME:
            case TIMESTAMP:
            case AUTO:
                return true;
            default:
                break;
        }

        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        switch (this.type) {
            case DECIMAL:
                return Objects.hash(type.getSqlValue(size),
                        this.digits == null ? DEFAULT_DIGITS : this.digits,
                        this.decimals == null ? DEFAULT_DECIMALS : this.decimals);
            case DOUBLE:
            case FLOAT:
                if (this.precision != null) return Objects.hash(type.getSqlValue(size), this.precision);
                break;
            case STRING:
            case TEXT:
            case BLOB:
            case INTEGER:
                if (this.length != null) return Objects.hash(this.type.getSqlValue(size), this.length);

                int defaultLength = 255;

                // TEXT and BLOB are biggg
                if (type == SqlType.TEXT || type == SqlType.BLOB) defaultLength = 65535;

                return Objects.hash(this.type.getSqlValue(size), defaultLength);
            case BOOLEAN:
            case DATE:
            case DATETIME:
            case TIMESTAMP:
            case AUTO:
            default:
                break;
        }

        return Objects.hash(type.getSqlValue(size));
    }

    public static class Builder {
        private final SqlType type;

        private SqlType.Size size = null;
        private Integer length = null;
        private Integer precision = null;
        private Integer digits = null;
        private Integer decimals = null;

        public Builder(@Nonnull SqlType type) {
            this.type = type;
        }

        public Builder setSize(SqlType.Size size) {
            this.size = size;
            return this;
        }

        public Builder setLength(int length) {
            this.length = length;
            return this;
        }

        public Builder setPrecision(int precision) {
            this.precision = precision;
            return this;
        }

        public Builder setDigits(int digits) {
            this.digits = digits;
            return this;
        }

        public Builder setDecimals(int decimals) {
            this.decimals = decimals;
            return this;
        }

        public ColumnType build() {
            return new ColumnType(
                    type,
                    size,
                    length,
                    precision,
                    digits,
                    decimals
            );
        }
    }
}
