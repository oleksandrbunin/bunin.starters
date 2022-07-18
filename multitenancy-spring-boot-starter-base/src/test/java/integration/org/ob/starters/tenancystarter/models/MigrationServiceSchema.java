package integration.org.ob.starters.tenancystarter.models;

import integration.org.ob.starters.tenancystarter.migrations.BaseSchemaMigrationsService;
import integration.org.ob.starters.tenancystarter.migrations.IMigrationPathProvider;
import integration.org.ob.starters.tenancystarter.migrations.ISchemaManipulator;
import org.ob.starters.commonwebstarter.Tenant;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;

import javax.sql.DataSource;

public class MigrationServiceSchema extends BaseSchemaMigrationsService<DummyTenant> {

    protected final LiquibaseProperties tenantProperties;

    public MigrationServiceSchema(LiquibaseProperties defaultProperties,
                                  IMigrationPathProvider migrationPathProvider,
                                  ISchemaManipulator schemaManipulator,
                                  DataSource dataSource,
                                  LiquibaseProperties tenantProperties) {
        super(defaultProperties, migrationPathProvider, schemaManipulator, dataSource);
        this.tenantProperties = tenantProperties;
    }

    @Override
    protected Tenant getDefaultTenant() {
        return new DummyTenant("public");
    }

    @Override
    public void runMigrationsOnTenant(DummyTenant tenant) throws Exception {
        super.runMigrations(tenant, tenantProperties, migrationPathProvider.tenantsMigrationsPaths());
    }
}
