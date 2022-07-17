package org.ob.starters.commonwebstarter;

@FunctionalInterface
public interface ITenantIDResolver {
    String resolveTenant(String tenantId);
}
