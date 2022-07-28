package org.ob.starters.tenancystarter;

import io.vavr.control.Try;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ob.starters.tenancystarter.configuration.EmptyConfiguration;
import org.ob.starters.tenancystarter.configuration.MigrationServiceTestConfiguration;
import org.ob.starters.tenancystarter.configuration.SpringBootStarterTestConfiguration;
import org.ob.starters.tenancystarter.migrations.AutoCloseableLock;
import org.ob.starters.tenancystarter.migrations.BaseSchemaManipulator;
import org.ob.starters.tenancystarter.migrations.ISchemaManipulator;
import org.ob.starters.tenancystarter.migrations.utils.LockUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
        EmptyConfiguration.class,
        TenancyStarterConfiguration.class,
        YamlEnvironmentBeanProcessor.class,
        SpringBootStarterTestConfiguration.class,
        MigrationServiceTestConfiguration.class
})
@Tag("integration")
@ExtendWith(SpringExtension.class)
class BaseSchemaManipulatorTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    @Qualifier("cachingSchemaManipulator")
    ISchemaManipulator schemaManipulator;

    @Autowired
    JdbcTemplate jdbcTemplate;

    static final String DUMMY_SCHEMA_NAME_1 = UUID.randomUUID().toString();
    static final String DUMMY_SCHEMA_NAME_2 = UUID.randomUUID().toString();
    static final String MANIPULATION_TEST_LOCK_SCHEMA = UUID.randomUUID().toString();
    static final String TEST_LOCK_SCHEMA = UUID.randomUUID().toString();

    @BeforeAll
    static void init(@Qualifier("cachingSchemaManipulator") @Autowired ISchemaManipulator schemaManipulator) {
        List.of(DUMMY_SCHEMA_NAME_1, DUMMY_SCHEMA_NAME_2).forEach(schema -> Try.run(() -> schemaManipulator.createSchema(schema)));
    }

    @AfterAll
    static void clean(@Qualifier("cachingSchemaManipulator") @Autowired ISchemaManipulator schemaManipulator) {
        List.of(DUMMY_SCHEMA_NAME_1, DUMMY_SCHEMA_NAME_2).forEach(schema -> Try.run(() -> schemaManipulator.deleteSchema(schema)));
    }

    @Test
    void schemaMigrationsTest() {
        assertThat(Try.of(() -> schemaManipulator.existsSchema(DUMMY_SCHEMA_NAME_1)).get()).isTrue();
        assertThat(Try.of(() -> schemaManipulator.existsSchema(DUMMY_SCHEMA_NAME_2)).get()).isTrue();
    }

    @Test
    void schemaManipulationLockTest() throws SQLException {
        String sql = "SELECT locks.locktype as type, locks.objid as id FROM pg_catalog.pg_locks locks WHERE locks.locktype='advisory' AND locks.objid=%d".formatted(LockUtils.calculateLockId(MANIPULATION_TEST_LOCK_SCHEMA));

        schemaManipulator.createSchema(MANIPULATION_TEST_LOCK_SCHEMA);
        assertThat(jdbcTemplate.query(sql, (ResultSetExtractor<Boolean>) rs -> !rs.next())).isTrue();

        schemaManipulator.deleteSchema(MANIPULATION_TEST_LOCK_SCHEMA);
        assertThat(jdbcTemplate.query(sql, (ResultSetExtractor<Boolean>) rs -> !rs.next())).isTrue();
    }

    @RepeatedTest(3)
    void schemaLockTest() {
        DataSource dataSource = jdbcTemplate.getDataSource();
        logger.info("Expected ID is {}", TEST_LOCK_SCHEMA.hashCode());
        String sql = "SELECT locks.locktype as type, locks.objid as id FROM pg_catalog.pg_locks locks WHERE locks.locktype='advisory' AND locks.objid=%d".formatted(LockUtils.calculateLockId(TEST_LOCK_SCHEMA));
        try (Connection connection = dataSource.getConnection();
             AutoCloseableLock lock = BaseSchemaManipulator.makeSchemaLock(TEST_LOCK_SCHEMA, connection)) {
            // --- check that lock has not been acquired yet ---
            assertThat(jdbcTemplate.query(sql, (ResultSetExtractor<Boolean>) rs -> !rs.next())).isTrue();

            logger.info("Trying acquire the Lock");
            lock.lock();
            // --- check that correct lock must be acquired ---
            assertThat(
                    jdbcTemplate.query(sql, new CustomResultSetExtractor(TEST_LOCK_SCHEMA))
            ).isTrue();

        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

        // --- check that lock has been released ---
        assertThat(jdbcTemplate.query(sql, (ResultSetExtractor<Boolean>) rs -> !rs.next())).isTrue();

    }

    static class CustomResultSetExtractor implements ResultSetExtractor<Boolean> {

        private final String schema;

        public CustomResultSetExtractor(String schema) {
            this.schema = schema;
        }

        @Override
        public Boolean extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (!rs.next()) {
                logger.error("There is not any advisory locks");
                return false;
            }
            logger.info("Checking the lock's ID");
            long id = rs.getLong("id");
            logger.info("ID of the Lock is {}, expected ID = {}", id, schema.hashCode());
            return id == LockUtils.calculateLockId(schema);
        }
    }

}
