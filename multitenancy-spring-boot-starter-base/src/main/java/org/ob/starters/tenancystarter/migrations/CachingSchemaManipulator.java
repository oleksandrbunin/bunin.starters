package org.ob.starters.tenancystarter.migrations;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class CachingSchemaManipulator extends BaseSchemaManipulator {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final Object o = new Object();

    private final DataSource dataSource;
    private final Cache<String, Object> cache;

    protected CachingSchemaManipulator(JdbcTemplate jdbcTemplate,
                                    DataSource dataSource,
                                    @Nullable String schemaLoadPattern) {
        super(jdbcTemplate);
        this.dataSource = dataSource;
        cache = CacheBuilder.newBuilder()
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .initialCapacity(15)
                .maximumSize(1000)
                .build();
        if (schemaLoadPattern == null) {
            schemaLoadPattern = "%";
        }
        super.loadSchemasSimilarTo(schemaLoadPattern).forEach(schema -> cache.put(schema, o));
    }

    public CachingSchemaManipulator(JdbcTemplate jdbcTemplate,
                                    DataSource dataSource) {
        this(jdbcTemplate, dataSource, "%");
    }


    @Override
    public void createSchema(String schema) throws SQLException {
        createSchema(schema, true);
    }

    @Override
    public void deleteSchema(String schema) throws SQLException {
        this.deleteSchema(schema, true, true);
    }

    @Override
    public void createSchema(String schema, boolean ifNotExists) {
        if (cache.getIfPresent(schema) != null) return;
        try (Connection connection = dataSource.getConnection();
             AutoCloseableLock lock = BaseSchemaManipulator.makeSchemaLock(schema, connection)) {
            logger.info("Trying to acquire lock on {} schema for creation", schema);
            lock.lock();
            super.createSchema(schema, ifNotExists);
            cache.put(schema, o);
        } catch (Exception exception) {
            logger.error("Exception occurred", exception);
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void deleteSchema(String schema, boolean cascade, boolean ifExists) {
        if (cache.getIfPresent(schema) == null) return;
        try (Connection connection = dataSource.getConnection();
             AutoCloseableLock lock = BaseSchemaManipulator.makeSchemaLock(schema, connection)) {
            logger.info("Trying to acquire lock on {} schema for deletion", schema);
            lock.lock();
            super.deleteSchema(schema, cascade, ifExists);
            cache.invalidate(schema);
        } catch (Exception exception) {
            logger.error("Exception occurred", exception);
            throw new RuntimeException(exception);
        }
    }

    @Override
    public boolean existsSchema(String schema) {
        if (cache.getIfPresent(schema) != null) {
            return true;
        }
        final boolean existsSchema = super.existsSchema(schema);
        if (existsSchema) {
            cache.put(schema, o);
        }
        return existsSchema;
    }

    @Override
    public Set<String> loadSchemasSimilarTo(@Nullable String regexPattern) {
        Set<String> schemas = super.loadSchemasSimilarTo(regexPattern);
        schemas.forEach(schema -> cache.put(schema, o));
        return schemas;
    }
}
