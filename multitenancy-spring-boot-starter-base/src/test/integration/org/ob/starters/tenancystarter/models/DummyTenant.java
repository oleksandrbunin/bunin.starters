package org.ob.starters.tenancystarter.models;

import org.ob.starters.commonwebstarter.Tenant;

public class DummyTenant implements Tenant {

    private String id;

    public DummyTenant(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isActive() {
        return true;
    }
}
