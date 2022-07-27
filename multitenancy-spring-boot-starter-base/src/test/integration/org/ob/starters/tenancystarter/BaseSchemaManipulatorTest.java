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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
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
        schemaManipulator.createSchema(MANIPULATION_TEST_LOCK_SCHEMA);
        assertThat(jdbcTemplate.query("SELECT locks.locktype as type, locks.objid as id FROM pg_catalog.pg_locks locks WHERE locks.locktype='advisory'", new ResultSetExtractor<Boolean>() {
            @Override
            public Boolean extractData(ResultSet rs) throws SQLException, DataAccessException {
                return !rs.next();
            }
        })).isTrue();

        schemaManipulator.deleteSchema(MANIPULATION_TEST_LOCK_SCHEMA);
        assertThat(jdbcTemplate.query("SELECT locks.locktype as type, locks.objid as id FROM pg_catalog.pg_locks locks WHERE locks.locktype='advisory'", new ResultSetExtractor<Boolean>() {
            @Override
            public Boolean extractData(ResultSet rs) throws SQLException, DataAccessException {
                return !rs.next();
            }
        })).isTrue();
    }

    @Test
    void schemaLockTest() throws SQLException {
        DataSource dataSource = jdbcTemplate.getDataSource();
        try (Connection connection = dataSource.getConnection();
             AutoCloseableLock lock = BaseSchemaManipulator.makeSchemaLock(TEST_LOCK_SCHEMA, connection)) {
            // --- check that lock has not been acquired yet ---
            assertThat(jdbcTemplate.query("SELECT locks.locktype as type, locks.objid as id FROM pg_catalog.pg_locks locks WHERE locks.locktype='advisory'", new ResultSetExtractor<Boolean>() {
                @Override
                public Boolean extractData(ResultSet rs) throws SQLException, DataAccessException {
                    return !rs.next();
                }
            })).isTrue();

            lock.lock();
            // --- check that correct lock must be acquired ---
            assertThat(jdbcTemplate.query("SELECT locks.locktype as type, locks.objid as id FROM pg_catalog.pg_locks locks WHERE locks.locktype='advisory'",
                    new ResultSetExtractor<Boolean>() {
                            @Override
                            public Boolean extractData(ResultSet rs) throws SQLException, DataAccessException {
                                if (!rs.next()) {
                                    return false;
                                }
                                return rs.getLong("id") == TEST_LOCK_SCHEMA.hashCode();
                            }
            })).isTrue();

        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

        // --- check that lock has been released ---
        assertThat(jdbcTemplate.query("SELECT locks.locktype as type, locks.objid as id FROM pg_catalog.pg_locks locks WHERE locks.locktype='advisory'", new ResultSetExtractor<Boolean>() {
            @Override
            public Boolean extractData(ResultSet rs) throws SQLException, DataAccessException {
                return !rs.next();
            }
        })).isTrue();

    }

}
