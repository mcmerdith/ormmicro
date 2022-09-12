package net.mcmerdith.ormmicro.exceptions;

public class SqlConstraintViolation extends RuntimeException {
    public SqlConstraintViolation(String message) {
        super("SQL Constraint Violation: " + message);
    }

    public SqlConstraintViolation(String message, Throwable cause) {
        super("SQL Constraint Violation: " + message, cause);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
