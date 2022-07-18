package integration.org.ob.starters.tenancystarter;

import integration.org.ob.starters.tenancystarter.configuration.EmptyConfiguration;
import integration.org.ob.starters.tenancystarter.configuration.MigrationServiceTestConfiguration;
import integration.org.ob.starters.tenancystarter.configuration.SpringBootStarterTestConfiguration;
import integration.org.ob.starters.tenancystarter.models.DummyTenant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.ob.starters.tenancystarter.MultitenancyStarterConfiguration;
import org.ob.starters.tenancystarter.YamlEnvironmentBeanProcessor;
import org.ob.starters.tenancystarter.migrations.ISchemaManipulator;
import org.ob.starters.tenancystarter.migrations.ISchemaMigrationsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.lang.invoke.MethodHandles;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
        EmptyConfiguration.class,
        MultitenancyStarterConfiguration.class,
        YamlEnvironmentBeanProcessor.class,
        SpringBootStarterTestConfiguration.class,
        MigrationServiceTestConfiguration.class
})
class MigrationServiceTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    ISchemaMigrationsService<DummyTenant> migrationServiceSchema;

    @Autowired
    ISchemaManipulator schemaManipulator;

    @Autowired
    JdbcTemplate jdbcTemplate;
    static final String DUMMY_SCHEMA_NAME_1 = "dummytenant1";
    static final String DUMMY_SCHEMA_NAME_2 = "dummytenant2";

    @BeforeEach
    void init() {
        jdbcTemplate.execute("DROP SCHEMA public CASCADE; CREATE SCHEMA public; GRANT ALL ON SCHEMA public TO postgres; GRANT ALL ON SCHEMA public TO public;");
        List.of(DUMMY_SCHEMA_NAME_1, DUMMY_SCHEMA_NAME_2).forEach(schemaManipulator::createSchema);
    }

    @AfterEach
    void clean() {
        List.of(DUMMY_SCHEMA_NAME_1, DUMMY_SCHEMA_NAME_2).forEach(schemaManipulator::deleteSchema);
        jdbcTemplate.execute("DROP SCHEMA public CASCADE; CREATE SCHEMA public; GRANT ALL ON SCHEMA public TO postgres; GRANT ALL ON SCHEMA public TO public;");
    }

    @Test
    void alwaysTrue() {
        assertThat(1 + 1).isEqualTo(2);
    }

    @Test
    void schemaMigrationsTest() {
        DummyTenant dummyTenant1 = new DummyTenant(DUMMY_SCHEMA_NAME_1);
        DummyTenant dummyTenant2 = new DummyTenant(DUMMY_SCHEMA_NAME_2);

        try {
            ISchemaMigrationsService<DummyTenant> dummyTenantISchemaMigrationsService = migrationServiceSchema;
            for (DummyTenant dummyTenant : List.of(dummyTenant1, dummyTenant2)) {
                dummyTenantISchemaMigrationsService.runMigrationsOnTenant(dummyTenant);
            }
            dummyTenantISchemaMigrationsService.runMigrationsOnDefaultTenant();
        } catch (Exception exception) {
            logger.error(exception, () -> "Exception occurred");
        }

        List.of(DUMMY_SCHEMA_NAME_1, DUMMY_SCHEMA_NAME_2, "public").forEach(schema -> assertThat(
                jdbcTemplate.query("SELECT COUNT(t) FROM \"%s\".test t".formatted(schema),
                        (ResultSetExtractor<Integer>) rs -> {
                            if (rs.next()) {
                                return rs.getInt(1);
                            }
                            return 0;
                        })
        ).isEqualTo(1));

    }


}
