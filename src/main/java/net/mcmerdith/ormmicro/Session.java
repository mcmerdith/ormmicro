package net.mcmerdith.ormmicro;

import java.sql.Connection;

public class Session implements AutoCloseable {
    private final SessionFactory sessionFactory;

    private final Connection connection;

    private final ModelManager modelManager;

    private boolean transaction = false;

    public Session(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;

        modelManager = sessionFactory.getModelManager();
        try {
            connection = sessionFactory.getDataSource().getConnection();
        } catch (Exception e) {
            throw new RuntimeException("Could not create session, failed to connect to database!", e);
        }
    }

    /**
     * Begin a new transaction on the database
     * <p>If this session is already in a transaction, the existing transaction is committed before beginning</p>
     */
    public void beginTransaction() {
        if (transaction) commit();

        try {
            connection.setAutoCommit(false);
            transaction = true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to start transaction!", e);
        }
    }

    /**
     * Commit the existing transaction
     * <p>If this session is not in a transaction this call is a no-op</p>
     */
    public void commit() {
        if (!transaction) return;

        try {
            connection.commit();
            connection.setAutoCommit(true);
            transaction = false;
        } catch (Exception e) {
            throw new RuntimeException("Failed to commit transaction!", e);
        }
    }

    /**
     * Rollback the existing transaction
     * <p>If this session is not in a transaction this call is a no-op</p>
     */
    public void rollback() {
        if (!transaction) return;

        try {
            connection.rollback();
            connection.setAutoCommit(true);
            transaction = false;
        } catch (Exception e) {
            throw new RuntimeException("Failed to rollback transaction!", e);
        }
    }

    public void persist(Object o) {

    }

    public void merge(Object o) {

    }

    public void remove(Object o) {
        modelManager.getModel(o.getClass());
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}
