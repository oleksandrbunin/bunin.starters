package org.ob.starters.tenancystarter.migrations;

import org.springframework.jdbc.core.ConnectionCallback;

public interface ISchemaManipulatorSupplier {
    ConnectionCallback<Void> createSchema(String schema, boolean ifNotExists);

    ConnectionCallback<Void> deleteSchema(String schema, boolean cascade, boolean ifExists);

    ConnectionCallback<Boolean> existsSchema(String schema);
}
