package ie.setu.hcs.util;

import ie.setu.hcs.config.DatabaseConfig;
import ie.setu.hcs.exception.DataAccessException;

import java.sql.Connection;
import java.sql.SQLException;

public final class TransactionRunner {

    @FunctionalInterface
    public interface TransactionCallback<T> {
        T execute(Connection connection) throws Exception;
    }

    private TransactionRunner() {
    }

    public static <T> T inTransaction(TransactionCallback<T> callback) throws Exception {
        try (Connection connection = DatabaseConfig.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                T result = callback.execute(connection);
                connection.commit();
                return result;
            } catch (Exception ex) {
                rollbackQuietly(connection, ex);
                throw ex;
            } finally {
                restoreAutoCommit(connection, originalAutoCommit);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not execute the database transaction.", ex);
        }
    }

    private static void rollbackQuietly(Connection connection, Exception original) {
        try {
            connection.rollback();
        } catch (SQLException rollbackEx) {
            original.addSuppressed(rollbackEx);
        }
    }

    private static void restoreAutoCommit(Connection connection, boolean originalAutoCommit) throws DataAccessException {
        try {
            connection.setAutoCommit(originalAutoCommit);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not restore database auto-commit mode.", ex);
        }
    }
}
