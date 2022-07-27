package org.ob.starters.tenancystarter.configuration;

import org.ob.starters.tenancystarter.migrations.*;
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

    @Bean("baseSchemaManipulator")
    public static ISchemaManipulator baseSchemaManipulator(JdbcTemplate jdbcTemplate) {
        return new BaseSchemaManipulator(jdbcTemplate);
    }

    @Bean("cachingSchemaManipulator")
    public static ISchemaManipulator cachingSchemaManipulator(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        return new CachingSchemaManipulator(jdbcTemplate, dataSource);
    }

    @Bean
    public ISchemaMigrationsService migrationsService(IMigrationPathProvider migrationPathProvider,
                                                      @Qualifier("baseSchemaManipulator")
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
