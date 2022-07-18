package org.ob.starters.tenancystarter;

import org.junit.jupiter.api.Test;
import org.ob.starters.tenancystarter.configuration.MigrationPathProviderConfiguration;
import org.ob.starters.tenancystarter.migrations.IMigrationPathProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
        org.ob.starters.tenancystarter.configuration.TestConfiguration.class,
        MultitenancyStarterConfiguration.class,
        YamlEnvironmentBeanProcessor.class,
        MigrationPathProviderConfiguration.class
})
class MigrationPathProviderTest extends BaseTest {

    @Autowired
    private IMigrationPathProvider migrationPathProvider;

    @Test
    void defaultSchemeMigrationsTest() {
        List<URI> migrations = migrationPathProvider.defaultMigrationsPaths();
        assertThat(migrations).hasSize(2);
        assertThat(
                migrations.stream().allMatch(uri -> uri.toString().contains("default_schema-changelog.xml"))
        ).isTrue();
    }

    @Test
    void tenantSchemeMigrationsTest() {
        List<URI> migrations = migrationPathProvider.tenantsMigrationsPaths();
        assertThat(migrations).hasSize(2);
        assertThat(
                migrations.stream().allMatch(uri -> uri.toString().contains("tenants-changelog.xml"))
        ).isTrue();
    }


}
