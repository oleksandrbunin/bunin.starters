package org.ob.starters.tenancystarter.migrations;

public interface ISchemaMigrationsService {
    void runMigrationsOnSchema(String schema) throws Exception;

    void runMigrationsOnDefaultSchema() throws Exception;
}
