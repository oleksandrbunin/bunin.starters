package org.ob.starters.tenancystarter.migrations;

import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import static org.ob.starters.tenancystarter.multitenancy.SpringLiquibaseBuilder.buildDefault;

public abstract class BaseSchemaMigrationsService implements ISchemaMigrationsService, ResourceLoaderAware {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final LiquibaseProperties defaultProperties;
    protected final IMigrationPathProvider migrationPathProvider;
    protected final ISchemaManipulator schemaManipulator;
    private final DataSource dataSource;
    private ResourceLoader resourceLoader;

    protected BaseSchemaMigrationsService(LiquibaseProperties defaultProperties,
                                          IMigrationPathProvider migrationPathProvider,
                                          ISchemaManipulator schemaManipulator,
                                          DataSource dataSource) {
        this.defaultProperties = defaultProperties;
        this.migrationPathProvider = migrationPathProvider;
        this.schemaManipulator = schemaManipulator;
        this.dataSource = dataSource;
    }


    /**
     * @throws IllegalArgumentException when default schema tenant does not exist
     */
    @Override
    public void runMigrationsOnDefaultSchema()
            throws LiquibaseException, IllegalArgumentException, SQLException {
        runMigrations(getDefaultSchema(), defaultProperties, migrationPathProvider.defaultMigrationsPaths());
    }

    @Override
    public void runMigrationsOnSchema(String schema) throws Exception {
        runMigrations(schema, defaultProperties, migrationPathProvider.tenantsMigrationsPaths());
    }

    /**
     * @throws IllegalArgumentException when schema does not exist
     */
    protected void runMigrations(@NotNull String schema,
                               @NotNull LiquibaseProperties liquibaseProperties,
                               @NotNull List<URI> migrationsUris)
            throws LiquibaseException, IllegalArgumentException, SQLException {
        logger.debug("CHECK THAT TENANT SCHEMA EXISTS");
        if(!schemaManipulator.existsSchema(schema)) {
            throw new IllegalArgumentException("Schema %s does not exist".formatted(schema));
        }
        logger.debug("RUNNING MIGRATIONS on {} schema", schema);
        for (URI migrationUri: migrationsUris) {
            String migrationPath = relativizeToClasspathMigrationUri(migrationUri);
            ResourceAccessor resourceAccessor = createResourceAccessor(migrationUri);
            logger.debug("STARTING MIGRATION {} on {} schema", migrationPath, schema);
            SpringLiquibase liquibase = buildDefault(
                    dataSource,
                    schema,
                    migrationPath,
                    resourceLoader,
                    resourceAccessor,
                    liquibaseProperties
            );
            liquibase.afterPropertiesSet();
            logger.debug("FINISHED MIGRATION {} on {} schema", migrationPath, schema);
        }
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    protected abstract String getDefaultSchema();


    ///
    ///     MIGRATIONS' PATHS ISSUES (NOT MY CODE):
    ///


    private String relativizeToClasspathMigrationUri(URI migrationsUri) {
        String migrationPath = migrationsUri.getPath();
        if (!isClasspathResource(migrationsUri.getScheme())) {
            // migration's path relative to jar root for example, <starter_name>.jar/starter-migrations.xml
            // 1. jar:file:/libs/starter.jar!/liquibase/migration.xml -> /libs/starter.jar/liquibase/migration.xml
            // 2. get Path to the classpath
            // 3. relativize to the classpath
            Path absolutePath = normalizePath(migrationsUri);
            Path classPath = absolutePath.getParent().getParent();
            migrationPath = classPath.relativize(absolutePath).toString();
        }
        return migrationPath;
    }

    private ResourceAccessor createResourceAccessor(URI migrationsUri) {
        Path absolutePath = normalizePath(migrationsUri);
        Path classPath = absolutePath.getParent().getParent();
        return new FileSystemResourceAccessor(classPath.toFile());
    }

    private boolean isClasspathResource(String uriScheme) {
        return "classpath".equals(uriScheme);
    }

    private static Path normalizePath(URI uri) {
        String path = uri.getPath() != null ? uri.getPath() : uri.toString();
        String migrationPath = path.replaceFirst("jar:file:", "").replaceFirst("jar!", "jar");
        migrationPath = migrationPath.replaceFirst("^/([A-Z]:/)", "$1");
        return Path.of(migrationPath);
    }
}
