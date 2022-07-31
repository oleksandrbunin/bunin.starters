package org.ob.starters.tenancystarter.multitenancy.tenantcrud;

import org.ob.starters.commonwebstarter.Tenant;

import java.util.Optional;

public interface ITenantOperations<T extends Tenant> {

    Optional<T> findById(String id);

    boolean existsById(String id);

    Iterable<T> findAll();

    T save(T tenant) throws Exception;

    T update(T tenant) throws Exception;

    void deleteById(String id);

    long count();

}
