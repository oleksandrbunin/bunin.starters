package org.ob.starters.tenancystarter.multitenancy;

import liquibase.resource.ResourceAccessor;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.core.io.ResourceLoader;

import javax.sql.DataSource;

public class SpringLiquibaseBuilder {

    private SpringLiquibaseBuilder() {
        // no-args constructor
    }

    public static TenancyStarterLiquibase buildDefault(DataSource dataSource,
                                                       String schema,
                                                       String migrationPath,
                                                       ResourceLoader resourceLoader,
                                                       ResourceAccessor resourceAccessor,
                                                       LiquibaseProperties liquibaseProperties) {
        TenancyStarterLiquibase liquibase = new TenancyStarterLiquibase(resourceAccessor);
        liquibase.setChangeLog(migrationPath);
        liquibase.setResourceLoader(resourceLoader);
        liquibase.setDataSource(dataSource);
        liquibase.setDefaultSchema(schema);
        liquibase.setContexts(liquibaseProperties.getContexts());
        liquibase.setLabels(liquibaseProperties.getLabels());
        liquibase.setDropFirst(liquibaseProperties.isDropFirst());
        liquibase.setClearCheckSums(liquibaseProperties.isClearChecksums());
        liquibase.setShouldRun(true);
        liquibase.setLiquibaseSchema(liquibaseProperties.getLiquibaseSchema());
        liquibase.setLiquibaseTablespace(liquibaseProperties.getLiquibaseTablespace());
        liquibase.setDatabaseChangeLogTable(liquibaseProperties.getDatabaseChangeLogTable());
        liquibase.setDatabaseChangeLogLockTable(liquibaseProperties.getDatabaseChangeLogLockTable());
        // https://github.com/liquibase/liquibase/issues/1651
        return liquibase;
    }

}
