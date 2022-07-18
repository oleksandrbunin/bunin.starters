package org.ob.starters.tenancystarter.multitenancy.tenantcrud;

import org.ob.starters.commonwebstarter.Tenant;

import java.util.Optional;

public interface ITenantReadRepository<T extends Tenant> {

    Optional<T> findById(String id);

    boolean existsById(String id);

    Iterable<T> findAll();

    long count();

}
