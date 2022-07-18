package org.ob.starters.tenancystarter.configuration;

import org.ob.starters.tenancystarter.migrations.IMigrationPathProvider;
import org.ob.starters.tenancystarter.migrations.ISchemaManipulator;
import org.ob.starters.tenancystarter.migrations.ISchemaMigrationsService;
import org.ob.starters.tenancystarter.migrations.MigrationPathsProvider;
import org.ob.starters.tenancystarter.models.DummyTenant;
import org.ob.starters.tenancystarter.models.MigrationServiceSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Configuration
@org.springframework.boot.test.context.TestConfiguration
public class MigrationServiceTestConfiguration {

    @Bean
    public IMigrationPathProvider migrationPathProvider() {
        return new MigrationPathsProvider(new PathMatchingResourcePatternResolver());
    }

    @Bean
    public ISchemaManipulator schemaManipulator(JdbcTemplate jdbcTemplate) {
        return new ISchemaManipulator() {

            private static final Object o = new Object();
            private final Set<String> schemas = Collections.synchronizedSet(new HashSet<>());

            @Override
            public void createSchema(String schema) {
                // since it is used only in tests, I enabled myself to use it as I want and not think much
                synchronized (o) {
                    jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS \"%s\"".formatted(schema));
                    schemas.add(schema);
                }
            }

            @Override
            public void deleteSchema(String schema) {
                // since it is used only in tests, I enabled myself to use it as I want and not think much
                synchronized (o) {
                    jdbcTemplate.execute("DROP SCHEMA IF EXISTS \"%s\" CASCADE".formatted(schema));
                    schemas.add(schema);
                }
            }

            @Override
            public boolean existsSchema(String schema) {
                return schemas.contains(schema) || Objects.equals(schema, "public");
            }
        };
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
