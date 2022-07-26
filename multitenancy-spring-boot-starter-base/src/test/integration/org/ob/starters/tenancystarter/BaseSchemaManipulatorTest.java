package org.ob.starters.tenancystarter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ob.starters.tenancystarter.configuration.EmptyConfiguration;
import org.ob.starters.tenancystarter.configuration.MigrationServiceTestConfiguration;
import org.ob.starters.tenancystarter.configuration.SpringBootStarterTestConfiguration;
import org.ob.starters.tenancystarter.migrations.ISchemaManipulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
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
class BaseSchemaManipulatorTest extends BaseTest {

    @Autowired
    ISchemaManipulator schemaManipulator;

    @Autowired
    JdbcTemplate jdbcTemplate;

    static final String DUMMY_SCHEMA_NAME_1 = UUID.randomUUID().toString();
    static final String DUMMY_SCHEMA_NAME_2 = UUID.randomUUID().toString();

    @BeforeEach
    void init() {
        List.of(DUMMY_SCHEMA_NAME_1, DUMMY_SCHEMA_NAME_2).forEach(schemaManipulator::createSchema);
    }

    @AfterEach
    void clean() {
        List.of(DUMMY_SCHEMA_NAME_1, DUMMY_SCHEMA_NAME_2).forEach(schemaManipulator::deleteSchema);
    }

    @Test
    void schemaMigrationsTest() {
        assertThat(schemaManipulator.existsSchema(DUMMY_SCHEMA_NAME_1)).isTrue();
        assertThat(schemaManipulator.existsSchema(DUMMY_SCHEMA_NAME_2)).isTrue();
    }


}
