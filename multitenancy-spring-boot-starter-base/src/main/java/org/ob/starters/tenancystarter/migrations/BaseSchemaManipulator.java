package org.ob.starters.tenancystarter.migrations;

import org.hibernate.cfg.NotYetImplementedException;
import org.ob.starters.tenancystarter.exceptions.LockReleaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;

import javax.annotation.Nullable;
import javax.persistence.LockTimeoutException;
import java.lang.invoke.MethodHandles;

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

    public BaseSchemaManipulator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
        if (ifNotExists) {
            jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS \"%s\"".formatted(schema));
        } else {
            jdbcTemplate.execute("CREATE SCHEMA \"%s\"".formatted(schema));
        }
        logger.info("Schema {} created", schema);
    }

    @Override
    public void deleteSchema(String schema, boolean cascade, boolean ifExists) throws SQLException {
        logger.info("Schema {} deletion", schema);
        String sql = "DROP SCHEMA IF EXISTS \"%s\" CASCADE";
        if (!cascade) {
            sql = sql.replace("CASCADE", "");
        }
        if (!ifExists) {
            sql = sql.replace("IF EXISTS", "");
        }
        jdbcTemplate.execute(sql.formatted(schema));
        logger.info("Schema {} deleted", schema);
    }

    @Override
    public boolean existsSchema(String schema) {
        // can be null
        return "public".equals(schema) ||
                Objects.equals(
                        jdbcTemplate.queryForObject(
                                "SELECT EXISTS(SELECT 1 FROM pg_namespace WHERE nspname = '%s')".formatted(schema),
                                Boolean.class
                        ), Boolean.TRUE
                );
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
    public Set<String> loadSchemasSimilarTo(@Nullable String regexPattern) {
        return loadAllSchemasSimilarTo(regexPattern).stream().filter(schema ->
                !(
                        "information_schema".equals(schema) || schema.contains("pg_")
                )
        ).collect(Collectors.toUnmodifiableSet());
    }

    protected Set<String> loadAllSchemasSimilarTo(@Nullable String regexPattern) {
        PreparedStatementCreator preparedStatementCreator = con -> {
            if (regexPattern != null) {
                PreparedStatement preparedStatement = con.prepareStatement(
                        "SELECT nspname FROM pg_namespace WHERE nspname SIMILAR TO ?"
                );
                preparedStatement.setString(1, regexPattern);
                return preparedStatement;
            } else {
                return con.prepareStatement("SELECT nspname FROM pg_namespace");
            }
        };
        return jdbcTemplate.query(preparedStatementCreator, rs -> {
            Set<String> results = new HashSet<>();
            while (rs.next()) {
                results.add(rs.getString("nspname"));
            }
            return results;
        });
    }

    public static AutoCloseableLock makeSchemaLock(String schema, Connection connection) {
        return new SchemaLock(connection, schema);
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

    private final Connection connection;
    private final String schema;
    private final int maxAttempts;

    SchemaLock(Connection connection, String schema, int maxAttempts) {
        this.connection = connection;
        this.schema = schema;
        this.maxAttempts = maxAttempts;
    }

    SchemaLock(Connection connection, String schema) {
        this.connection = connection;
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
        try {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            logger.info(
                    "TRYING TO LOCK SCHEMA {} FOR {} MS",
                    schema,
                    schemaLockTimeoutMs
            );
            try (Statement timeoutStatement = connection.createStatement()) {
                String setLockTimeoutSql = "/* schema: " + schema + " */ " + LOCK_TIMEOUT_QUERY + schemaLockTimeoutMs + ";";
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
        return schema.hashCode();
    }
}
