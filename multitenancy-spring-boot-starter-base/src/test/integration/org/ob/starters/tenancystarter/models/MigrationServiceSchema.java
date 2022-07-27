package org.ob.starters.tenancystarter.models;

import org.ob.starters.tenancystarter.migrations.BaseSchemaMigrationsService;
import org.ob.starters.tenancystarter.migrations.IMigrationPathProvider;
import org.ob.starters.tenancystarter.migrations.ISchemaManipulator;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;

import javax.sql.DataSource;

public class MigrationServiceSchema extends BaseSchemaMigrationsService {

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
    protected String getDefaultSchema() {
        return "public";
    }

    @Override
    public void runMigrationsOnSchema(String schema) throws Exception {
        super.runMigrations(schema, tenantProperties, migrationPathProvider.tenantsMigrationsPaths());
    }
}
