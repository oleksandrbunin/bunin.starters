package integration.org.ob.starters.tenancystarter;

import integration.org.ob.starters.tenancystarter.configuration.EmptyConfiguration;
import org.junit.jupiter.api.Test;
import integration.org.ob.starters.tenancystarter.configuration.MigrationPathProviderTestConfiguration;
import integration.org.ob.starters.tenancystarter.migrations.IMigrationPathProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
        EmptyConfiguration.class,
        MultitenancyStarterConfiguration.class,
        YamlEnvironmentBeanProcessor.class,
        MigrationPathProviderTestConfiguration.class
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
