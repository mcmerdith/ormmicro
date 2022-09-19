package net.mcmerdith.ormmicro.internal;

import net.mcmerdith.ormmicro.OrmMicroLogger;
import net.mcmerdith.ormmicro.query.SqlQuery;
import net.mcmerdith.ormmicro.util.SqlUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class DatabaseWorker extends TimerTask {
    private static class DatabaseTask {
        public final String statement;
        public final List<Object> parameters;
        public final Consumer<ResultSet> receiver;
        public final Consumer<Integer> updateReceiver;

        public DatabaseTask(String statement, List<Object> parameters, Consumer<ResultSet> receiver, Consumer<Integer> updateReceiver) {
            this.statement = statement;
            this.parameters = parameters;
            this.receiver = receiver;
            this.updateReceiver = updateReceiver;
        }
    }

    private final SessionFactory sessionFactory;

    private final ConcurrentLinkedQueue<DatabaseTask> taskQueue = new ConcurrentLinkedQueue<>();

    public DatabaseWorker(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void run() {
        if (taskQueue.isEmpty()) return;

        Connection connection = sessionFactory.getCurrentSession().getConnection();

        while(!taskQueue.isEmpty()) {
            DatabaseTask currentTask = taskQueue.poll();
            if (currentTask == null) continue;

            PreparedStatement statement;
            boolean results;
            try {
                statement = connection.prepareStatement(currentTask.statement);
                SqlUtil.insertParametersInto(statement, currentTask.parameters);

                results = statement.execute();
            } catch (SQLException e) {
                OrmMicroLogger.DATABASE_WORKER.exception(e, "Failed to execute async statement `" + currentTask.statement + "`");
                if (currentTask.receiver != null) currentTask.receiver.accept(null);
                if (currentTask.updateReceiver != null) currentTask.updateReceiver.accept(-1);
                continue;
            }

            try {
                if (currentTask.receiver != null) {
                    if (results) currentTask.receiver.accept(statement.getResultSet());
                    else currentTask.receiver.accept(null);
                }
            } catch (SQLException ignored) {
                currentTask.receiver.accept(null);
            }

            try {
                if (currentTask.updateReceiver != null) {
                    if (results) currentTask.updateReceiver.accept(-1);
                    else currentTask.updateReceiver.accept(statement.getUpdateCount());
                }
            } catch (SQLException ignored) {
                currentTask.updateReceiver.accept(-1);
            }
        }
    }

    public void executeUpdateAsync(String statement, List<Object> parameters, Consumer<Integer> receiver) {
        taskQueue.add(new DatabaseTask(statement, parameters, null, receiver));
    }

    public void executeQueryAsync(String statement, List<Object> parameters, Consumer<ResultSet> receiver) {
        taskQueue.add(new DatabaseTask(statement, parameters, receiver, null));
    }
}
