package org.ob.starters.tenancystarter.multitenancy.tenantcrud;

import org.ob.starters.commonwebstarter.Tenant;

import javax.annotation.Nullable;

public interface ITenantManagementService<T extends Tenant> {
    Tenant createTenant(@Nullable T tenant);

    Tenant removeTenant(T tenant);
}
