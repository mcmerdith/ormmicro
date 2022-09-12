package net.mcmerdith.ormmicro.exceptions;

public class DatabaseConnectionException extends RuntimeException {
    public DatabaseConnectionException(String message) {
        super("Database Connection Error: " + message);
    }

    public DatabaseConnectionException(String message, Throwable cause) {
        super("Database Connection Error: " + message, cause);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return super.fillInStackTrace();
    }
}
