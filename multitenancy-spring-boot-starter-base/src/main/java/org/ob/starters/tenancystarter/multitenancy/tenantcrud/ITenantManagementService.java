package org.ob.starters.tenancystarter.multitenancy.tenantcrud;

import org.ob.starters.commonwebstarter.Tenant;

public interface ITenantManagementService<T extends Tenant> {
    Tenant createTenant();

    Tenant removeTenant(T tenant);
}
