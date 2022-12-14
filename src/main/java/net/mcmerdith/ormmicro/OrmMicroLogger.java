package net.mcmerdith.ormmicro;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OrmMicroLogger {
    private static Map<String, OrmMicroLogger> LOGGERS = new HashMap<>();

    public static OrmMicroLogger QUERY_BUILDER = instance("Query Builder");
    public static OrmMicroLogger MODEL_MAPPER = instance("Model Mapper");
    public static OrmMicroLogger DATABASE_WORKER = instance("Database Worker");

    public static OrmMicroLogger instance(String name) {
        if (!LOGGERS.containsKey(name)) LOGGERS.put(name, new OrmMicroLogger(name));
        return LOGGERS.get(name);
    }

    private final String name;

    private OrmMicroLogger(String name) {
        this(name, Logger.getLogger("OrmMicro"));
    }

    private OrmMicroLogger(String name, Logger parentLogger) {
        this.name = name;
        this.parentLogger = parentLogger;
    }

    private Logger parentLogger;

    public void setLogger(Logger logger) {
        this.parentLogger = logger;
    }

    public void setLogLevel(Level level) {
        this.parentLogger.setLevel(level);
    }

    public static void setLogLevels(Level level) {
        for (OrmMicroLogger logger : LOGGERS.values()) {
            logger.setLogLevel(level);
        }
    }

    /**
     * Log an error
     *
     * @param error The error
     */
    public void error(String error) {
        exception(null, error);
    }

    /**
     * Log a non-fatal exception
     *
     * @param e The exception
     */
    public void exception(Exception e) {
        exception(e, null);
    }

    /**
     * Log a non-fatal exception
     *
     * @param e     The exception
     * @param cause A more detailed cause for the error
     */
    @SuppressWarnings("ThrowableNotThrown") // method with return is only used for convenience
    public void exception(Exception e, String cause) {
        exception(e, cause, false);
    }

    /**
     * Log an exception
     *
     * @param e     The exception
     * @param fatal If the exception is fatal
     * @return If fatal, an {@link IllegalStateException} with <code>exception</code> as the cause
     */
    public RuntimeException exception(Exception e, boolean fatal) {
        return exception(e, null, fatal);
    }

    /**
     * Log an exception
     *
     * @param exception The exception. If provided, {@link Exception#getCause()} is recursively searched and logged
     * @param fatal     If the exception is fatal
     * @param cause     A more detailed cause for the error
     * @return If fatal, a {@link RuntimeException} with <code>cause</code> as the message (or <code>{@link Exception#getMessage()}</code> if null), or null if not fatal
     */
    public RuntimeException exception(Exception exception, String cause, boolean fatal) {
        String nonNullException = (exception == null) ? "" : exception.getClass().getSimpleName() + ": " + exception.getMessage();

        String mainMessage = (cause == null) ? nonNullException : cause;
        String detailMessage = (cause == null || exception == null) ? "" : nonNullException;

        Level level = (fatal) ? Level.SEVERE : Level.WARNING;

        log(String.format("(%s) %s%s",
                level.getName(),
                mainMessage,
                detailMessage.trim().isEmpty() ? "" : String.format(" (%s)", detailMessage)
        ), level);

        // Log only the message of the causes of the exception
        // If we throw the exception normally, the stacktrace is super long and unreadable
        Throwable currentCause = (exception == null) ? null : exception.getCause();
        while (currentCause != null) {
            log("   Caused by " + currentCause.getClass().getSimpleName() + ": " + currentCause.getMessage(), level);
            currentCause = currentCause.getCause();
        }

        // If we're throwing the exception, provide a wrapped exception without the causes (we just logged them, don't do it twice)
        return (fatal) ? new RuntimeException(mainMessage) : null;
    }

    /**
     * Log a message at {@link Level#INFO}
     *
     * @param message The message to log
     */
    public void info(String message) {
        log(message, Level.INFO);
    }

    /**
     * Log a message
     *
     * @param message The message to log
     * @param level   The log level
     */
    public void log(String message, Level level) {
        parentLogger.log(level, String.format("[OrmMicro] %s%s", (name == null) ? "" : "[" + name + "] ", message));
    }

    public void debug(String message) {
        info("[debug] " + message);
    }
}
