package org.ob.starters.tenancystarter.multitenancy;

import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;

public class CustomLiquibaseProperties extends LiquibaseProperties {

    private String schemaChangeLogPath;

    public String getSchemaChangeLogPath() {
        return schemaChangeLogPath;
    }

    public void setSchemaChangeLogPath(String schemaChangeLogPath) {
        this.schemaChangeLogPath = schemaChangeLogPath;
    }
}
