package net.mcmerdith.ormmicro.typing;

public interface ISqlTypeMapper {
    ColumnType javaToSqlType(Class<?> java);
}
