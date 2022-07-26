package org.ob.starters.tenancystarter.configuration;

import org.ob.starters.tenancystarter.migrations.IMigrationPathProvider;
import org.ob.starters.tenancystarter.migrations.ISchemaManipulator;
import org.ob.starters.tenancystarter.migrations.ISchemaMigrationsService;
import org.ob.starters.tenancystarter.migrations.MigrationPathsProvider;
import org.ob.starters.tenancystarter.migrations.SchemaManipulator;
import org.ob.starters.tenancystarter.models.DummyTenant;
import org.ob.starters.tenancystarter.models.MigrationServiceSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
@TestConfiguration
public class MigrationServiceTestConfiguration {

    @Bean
    public IMigrationPathProvider migrationPathProvider() {
        return new MigrationPathsProvider(new PathMatchingResourcePatternResolver());
    }

    @Bean
    public ISchemaManipulator schemaManipulator(JdbcTemplate jdbcTemplate) {
        return new SchemaManipulator(jdbcTemplate);
    }

    @Bean
    public ISchemaMigrationsService<DummyTenant> migrationsService(IMigrationPathProvider migrationPathProvider,
                                                                   ISchemaManipulator schemaManipulator,
                                                                   DataSource dataSource,
                                                                   @Qualifier("defaultLiquibaseProperties")
                                                          @Autowired LiquibaseProperties defaultProperties,
                                                                   @Qualifier("tenancyLiquibaseProperties")
                                                          @Autowired LiquibaseProperties tenancyProperties) {
        return new MigrationServiceSchema(
                defaultProperties,
                migrationPathProvider,
                schemaManipulator,
                dataSource,
                tenancyProperties
        );
    }

}
