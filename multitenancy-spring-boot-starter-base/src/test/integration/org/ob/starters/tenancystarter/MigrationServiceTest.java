package org.ob.starters.tenancystarter;

import io.vavr.control.Try;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ob.starters.tenancystarter.configuration.EmptyConfiguration;
import org.ob.starters.tenancystarter.configuration.MigrationServiceTestConfiguration;
import org.ob.starters.tenancystarter.configuration.SpringBootStarterTestConfiguration;
import org.ob.starters.tenancystarter.migrations.ISchemaManipulator;
import org.ob.starters.tenancystarter.migrations.ISchemaMigrationsService;
import org.ob.starters.tenancystarter.models.DummyTenant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.lang.invoke.MethodHandles;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
        EmptyConfiguration.class,
        TenancyStarterConfiguration.class,
        YamlEnvironmentBeanProcessor.class,
        SpringBootStarterTestConfiguration.class,
        MigrationServiceTestConfiguration.class
})
@Tag("integration")
class MigrationServiceTest extends BaseTest {

    @Autowired
    ISchemaMigrationsService migrationServiceSchema;

    @Autowired
    @Qualifier("cachingSchemaManipulator")
    ISchemaManipulator schemaManipulator;

    @Autowired
    JdbcTemplate jdbcTemplate;
    static final String DUMMY_SCHEMA_NAME_1 = "dummytenant1";
    static final String DUMMY_SCHEMA_NAME_2 = "dummytenant2";

    @BeforeEach
    void init() {
        jdbcTemplate.execute("DROP SCHEMA public CASCADE; CREATE SCHEMA public; GRANT ALL ON SCHEMA public TO postgres;");
        List.of(DUMMY_SCHEMA_NAME_1, DUMMY_SCHEMA_NAME_2).forEach(schema -> Try.run(() -> schemaManipulator.createSchema(schema)));
    }

    @AfterEach
    void clean() {
        List.of(DUMMY_SCHEMA_NAME_1, DUMMY_SCHEMA_NAME_2).forEach(schema -> Try.run(() -> schemaManipulator.deleteSchema(schema)));
        jdbcTemplate.execute("DROP SCHEMA public CASCADE; CREATE SCHEMA public; GRANT ALL ON SCHEMA public TO postgres;");
    }

    @Test
    void alwaysTrue() {
        assertThat(1 + 1).isEqualTo(2);
    }

    @Test
    void schemaMigrationsTest() throws Exception {
        DummyTenant dummyTenant1 = new DummyTenant(DUMMY_SCHEMA_NAME_1);
        DummyTenant dummyTenant2 = new DummyTenant(DUMMY_SCHEMA_NAME_2);

        ISchemaMigrationsService dummyTenantISchemaMigrationsService = migrationServiceSchema;
        for (DummyTenant dummyTenant : List.of(dummyTenant1, dummyTenant2)) {
            dummyTenantISchemaMigrationsService.runMigrationsOnSchema(dummyTenant.getSchema());
        }
        dummyTenantISchemaMigrationsService.runMigrationsOnDefaultSchema();

        List.of(DUMMY_SCHEMA_NAME_1, DUMMY_SCHEMA_NAME_2).forEach(schema -> assertThat(
                jdbcTemplate.query("SELECT COUNT(t) FROM \"%s\".test t".formatted(schema),
                        (ResultSetExtractor<Integer>) rs -> {
                            if (rs.next()) {
                                return rs.getInt(1);
                            }
                            return 0;
                        })
        ).isEqualTo(1));

        List.of("public").forEach(schema -> assertThat(
                jdbcTemplate.query("SELECT COUNT(t) FROM \"%s\".defaultTestTable t".formatted(schema),
                        (ResultSetExtractor<Integer>) rs -> {
                            if (rs.next()) {
                                return rs.getInt(1);
                            }
                            return 0;
                        })
        ).isEqualTo(1));



    }


}
