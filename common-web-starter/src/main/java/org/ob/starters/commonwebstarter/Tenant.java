package org.ob.starters.commonwebstarter;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as=Tenant.class)
public interface Tenant {

    String getId();

    boolean isActive();

    default String getSchema() {
        return getId();
    }

}
