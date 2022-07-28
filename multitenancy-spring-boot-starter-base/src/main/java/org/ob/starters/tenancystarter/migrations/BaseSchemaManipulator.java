package org.ob.starters.tenancystarter.migrations;

import org.hibernate.cfg.NotYetImplementedException;
import org.ob.starters.tenancystarter.exceptions.LockReleaseException;
import org.ob.starters.tenancystarter.migrations.utils.LockUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Nullable;
import javax.persistence.LockTimeoutException;
import javax.validation.constraints.NotNull;
import java.lang.invoke.MethodHandles;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.stream.Collectors;

public class BaseSchemaManipulator implements ISchemaManipulator {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final JdbcTemplate jdbcTemplate;
    private final SchemaManipulatorSupplier schemaManipulatorSupplier;

    public BaseSchemaManipulator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        schemaManipulatorSupplier = new SchemaManipulatorSupplier();
    }

    @Override
    public void createSchema(String schema) throws SQLException {
        createSchema(schema, true);
    }

    @Override
    public void deleteSchema(String schema) throws SQLException {
        deleteSchema(schema, true, true);
    }

    @Override
    public void createSchema(String schema, boolean ifNotExists) throws SQLException {
        logger.info("Schema {} creation", schema);
        jdbcTemplate.execute(schemaManipulatorSupplier.createSchema(schema, ifNotExists));
        logger.info("Schema {} created", schema);
    }

    @Override
    public void deleteSchema(String schema, boolean cascade, boolean ifExists) throws SQLException {
        logger.info("Schema {} deletion", schema);
        jdbcTemplate.execute(schemaManipulatorSupplier.deleteSchema(schema, cascade, ifExists));
        logger.info("Schema {} deleted", schema);
    }

    @Override
    public boolean existsSchema(String schema) {
        // can be null
        return "public".equals(schema)
                ||
                Objects.equals(jdbcTemplate.execute(schemaManipulatorSupplier.existsSchema(schema)), Boolean.TRUE);
    }

    @Override
    public void createSchema(String schema, Connection connection) throws SQLException {
        createSchema(schema, true, connection);
    }

    @Override
    public void deleteSchema(String schema, Connection connection) throws SQLException {
        deleteSchema(schema, true, true, connection);
    }

    @Override
    public void createSchema(String schema, boolean ifNotExists, Connection connection) throws SQLException {
        logger.info("Schema {} creation", schema);
        schemaManipulatorSupplier.createSchema(schema, ifNotExists).doInConnection(connection);
        logger.info("Schema {} created", schema);
    }

    @Override
    public void deleteSchema(String schema, boolean cascade, boolean ifExists, Connection connection)
            throws SQLException {
        logger.info("Schema {} deletion", schema);
        schemaManipulatorSupplier.deleteSchema(schema, cascade, ifExists).doInConnection(connection);
        logger.info("Schema {} deleted", schema);
    }

    @Override
    public boolean existsSchema(String schema, Connection connection) throws SQLException {
        return Objects.requireNonNull(
                schemaManipulatorSupplier.existsSchema(schema).doInConnection(connection),
                "Exists schema query response returned null");
    }

    // --- EXTRA USEFUL METHODS ---

    /**
     * @param regexPattern sql regex pattern
     *
     *
     *  The SIMILAR TO operator returns true or false depending on whether its pattern matches the given string.
     *  It is similar to LIKE, except that it interprets the pattern using the SQL standard's definition of a regular expression.
     *  SQL regular expressions are a curious cross between LIKE notation and common (POSIX) regular expression notation.
     *
     *  Like LIKE, the SIMILAR TO operator succeeds only if its pattern matches the entire string;
     *  this is unlike common regular expression behavior where the pattern can match any part of the string.
     *  Also like LIKE, SIMILAR TO uses _ and % as wildcard characters denoting any single character and any string,
     *  respectively (these are comparable to . and .* in POSIX regular expressions).
     *
     */
    public Set<String> loadSchemasSimilarTo(@Nullable String regexPattern, Connection connection) throws SQLException {
        return Objects.requireNonNull(
                schemaManipulatorSupplier.loadSchemasSimilarTo(regexPattern).doInConnection(connection))
                .stream()
                .filter(schema -> !("information_schema".equals(schema) || schema.contains("pg_")))
                .collect(Collectors.toUnmodifiableSet());
    }

    public Set<String> loadSchemasSimilarTo(@Nullable String regexPattern) throws DataAccessException {
        return Objects.requireNonNull(
                jdbcTemplate.execute(schemaManipulatorSupplier.loadSchemasSimilarTo(regexPattern)))
                .stream()
                .filter(schema -> !("information_schema".equals(schema) || schema.contains("pg_")))
                .collect(Collectors.toUnmodifiableSet());
    }

    public static AutoCloseableLock makeSchemaLock(String schema, Connection connection) {
        return new SchemaLock(connection, schema);
    }
}

class SchemaManipulatorSupplier implements ISchemaManipulatorSupplier {

    @Override
    @NotNull
    public ConnectionCallback<Void> createSchema(String schema, boolean ifNotExists) {
        final String sql;
        if (ifNotExists) {
            sql = "CREATE SCHEMA IF NOT EXISTS \"%s\"".formatted(schema);
        } else {
            sql = "CREATE SCHEMA \"%s\"".formatted(schema);
        }
        return con -> {
            con.prepareStatement(sql).execute();
            return null;
        };
    }

    @Override
    @NotNull
    public ConnectionCallback<Void> deleteSchema(String schema, boolean cascade, boolean ifExists) {
        String prepareSqlQuery = "DROP SCHEMA IF EXISTS \"%s\" CASCADE";
        if (!cascade) {
            prepareSqlQuery = prepareSqlQuery.replace("CASCADE", "");
        }
        if (!ifExists) {
            prepareSqlQuery = prepareSqlQuery.replace("IF EXISTS", "");
        }
        prepareSqlQuery = prepareSqlQuery.formatted(schema);
        final String sql = prepareSqlQuery;
        return con -> {
            con.prepareStatement(sql).execute();
            return null;
        };
    }

    @Override
    @NotNull
    public ConnectionCallback<Boolean> existsSchema(String schema) {
        String sql = "SELECT EXISTS(SELECT 1 FROM pg_namespace WHERE nspname = '%s')".formatted(schema);
        return con -> {
            ResultSet resultSet = con.prepareStatement(sql).executeQuery();
            if (resultSet.next()) {
                return resultSet.getBoolean(1);
            } else {
                throw new SQLException("ResultSet does not contain results");
            }
        };
    }

    public ConnectionCallback<Set<String>> loadSchemasSimilarTo(@Nullable String regexPattern) {
        return con -> {
            final PreparedStatement preparedStatement;
            if (regexPattern != null) {
                preparedStatement = con.prepareStatement(
                        "SELECT nspname FROM pg_namespace WHERE nspname SIMILAR TO ?"
                );
                preparedStatement.setString(1, regexPattern);
            } else {
                preparedStatement = con.prepareStatement("SELECT nspname FROM pg_namespace");
            }

            ResultSet resultSet = preparedStatement.executeQuery();
            Set<String> results = new HashSet<>();
            while (resultSet.next()) {
                results.add(resultSet.getString("nspname"));
            }
            return results;
        };
    }
}

/**
 * Schema lock implementation
 * Throws LockTimeoutException
 */
class SchemaLock implements AutoCloseableLock  {

    private static final long defaultLockTimeout = 60;
    private static final TimeUnit defaultLockTimeUnit = TimeUnit.SECONDS;

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String LOCK_TIMEOUT_QUERY = "SET LOCAL lock_timeout TO ";
    private static final String LOCK_SCHEMA_QUERY = "SELECT pg_advisory_lock(?);";
    private static final String UNLOCK_SCHEMA_QUERY = "SELECT pg_advisory_unlock(?);";

    private final WeakReference<Connection> connectionWeakReference;
    private final String schema;
    private final int maxAttempts;

    SchemaLock(Connection connection, String schema, int maxAttempts) {
        this.connectionWeakReference = new WeakReference<>(connection);
        this.schema = schema;
        this.maxAttempts = maxAttempts;
    }

    SchemaLock(Connection connection, String schema) {
        this.connectionWeakReference = new WeakReference<>(connection);
        this.schema = schema;
        this.maxAttempts = 1;
    }

    @Override
    public void lock()  {
        int attempt = 0;
        while (attempt < maxAttempts) {
            logger.info("TRYING TO LOCK SCHEMA {} - {} ATTEMPT",
                    schema, attempt
            );
            try {
                if (tryLock()) {
                    return;
                }
            } catch (LockTimeoutException e) {
                // do nothing
                // continue make attempts
            }
            attempt++;
        }
        throw new LockTimeoutException("COULD NOT ACQUIRE LOCK FOR THE SCHEMA " + schema);
    }

    @Override
    public void lockInterruptibly() {
        throw new NotYetImplementedException();
    }

    @Override
    public boolean tryLock() {
        return tryLock(defaultLockTimeout, defaultLockTimeUnit);
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) {
        long schemaLockTimeoutMs = unit.toMillis(time);
        Connection connection = Objects.requireNonNull(
                this.connectionWeakReference.get(), "Connection was removed by GC"
        );
        try {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            logger.info(
                    "TRYING TO LOCK SCHEMA {} FOR {} MS",
                    schema,
                    schemaLockTimeoutMs
            );
            try (Statement timeoutStatement = connection.createStatement()) {
                String setLockTimeoutSql = "/* schema: " + schema + " */ " +
                        LOCK_TIMEOUT_QUERY + schemaLockTimeoutMs + ";";
                timeoutStatement.execute(setLockTimeoutSql);
                logger.info("LOCK WAS SET {}", setLockTimeoutSql);
            }
            String lockSchemaQuery = "/* schema: " + schema + " */ " + LOCK_SCHEMA_QUERY;
            try (PreparedStatement lockStatement = connection.prepareStatement(lockSchemaQuery)) {
                lockStatement.setInt(1, calculateLockForSchema(schema));
                lockStatement.execute();
                logger.info("LOCK WAS ACQUIRED {}", lockSchemaQuery);
            }
            connection.commit();
            connection.setAutoCommit(previousAutoCommit);
            return true;
        } catch (SQLException e) {
            try {
                logger.error("COULD NOT LOCK SCHEMA {} ", schema, e);
                connection.rollback();
            } finally {
                throw new LockTimeoutException("COULD NOT LOCK SCHEMA " + schema, e);
            }
        }
    }

    @Override
    public void unlock() {
        Connection connection = Objects.requireNonNull(
                this.connectionWeakReference.get(), "Connection was removed by GC"
        );
        try {
            logger.info("TRYING TO RELEASE {} SCHEMA LOCK", schema);
            String unlockTenantQuery = "/* schema: " + schema + " */ " + UNLOCK_SCHEMA_QUERY;
            try (PreparedStatement unlockStatement = connection.prepareStatement(unlockTenantQuery)) {
                unlockStatement.setInt(1, calculateLockForSchema(schema));
                try (ResultSet unlockResultSet = unlockStatement.executeQuery()) {
                    unlockResultSet.next();
                    if (!(Boolean) unlockResultSet.getObject(1)) {
                        logger.error("COULD NOT RELEASE LOCK FOR SCHEMA " + schema);
                        throw new LockReleaseException("COULD NOT RELEASE LOCK FOR SCHEMA " + schema);
                    }
                }
            }
            logger.info("SCHEMA {} LOCK WAS RELEASED", schema);
        } catch (SQLException e) {
            logger.error("COULD NOT RELEASE LOCK FOR SCHEMA " + schema, e);
            throw new LockReleaseException("COULD NOT RELEASE LOCK FOR SCHEMA " + schema, e);
        }
    }

    @Override
    public Condition newCondition() {
        throw new NotYetImplementedException();
    }

    @Override
    public void close() {
        unlock();
    }

    private int calculateLockForSchema(String schema) {
        return LockUtils.calculateLockId(schema);
    }
}
