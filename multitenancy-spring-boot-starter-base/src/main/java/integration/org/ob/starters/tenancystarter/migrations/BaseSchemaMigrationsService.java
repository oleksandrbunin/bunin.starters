package integration.org.ob.starters.tenancystarter.migrations;

import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.ob.starters.commonwebstarter.Tenant;
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
import java.util.List;

import static integration.org.ob.starters.tenancystarter.multitenancy.SpringLiquibaseBuilder.buildDefault;

public abstract class BaseSchemaMigrationsService<T extends Tenant> implements ISchemaMigrationsService<T>, ResourceLoaderAware {

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
    public void runMigrationsOnDefaultTenant()
            throws LiquibaseException, IllegalArgumentException {
        Tenant defaultTenant = getDefaultTenant();
        List<URI> defaultSchemaMigrationsUris = migrationPathProvider.defaultMigrationsPaths();
        runMigrations(defaultTenant, defaultProperties, defaultSchemaMigrationsUris);
    }

    /**
     * @throws IllegalArgumentException when tenant schema does not exist
     */
    protected void runMigrations(@NotNull Tenant tenant,
                               @NotNull LiquibaseProperties liquibaseProperties,
                               @NotNull List<URI> migrationsUris) throws LiquibaseException, IllegalArgumentException {
        logger.debug("CHECK THAT TENANT SCHEMA EXISTS");
        if(!schemaManipulator.existsSchema(tenant.getSchema())) {
            throw new IllegalArgumentException("Schema %s does not exist".formatted(tenant.getSchema()));
        }
        logger.debug("RUNNING MIGRATIONS on {} tenant", tenant);
        for (URI migrationUri: migrationsUris) {
            String migrationPath = relativizeToClasspathMigrationUri(migrationUri);
            ResourceAccessor resourceAccessor = createResourceAccessor(migrationUri);
            logger.debug("STARTING MIGRATION {} on {} tenant", migrationPath, tenant);
            SpringLiquibase liquibase = buildDefault(
                    dataSource,
                    tenant.getSchema(),
                    migrationPath,
                    resourceLoader,
                    resourceAccessor,
                    liquibaseProperties
            );
            liquibase.afterPropertiesSet();
            logger.debug("FINISHED MIGRATION {} on {} tenant", migrationPath, tenant);
        }
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    protected abstract Tenant getDefaultTenant();


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
