package integration.org.ob.starters.tenancystarter.migrations;

import org.ob.starters.commonwebstarter.Tenant;

public interface ISchemaMigrationsService<T extends Tenant> {
    void runMigrationsOnTenant(T tenant) throws Exception;

    void runMigrationsOnDefaultTenant() throws Exception;
}
