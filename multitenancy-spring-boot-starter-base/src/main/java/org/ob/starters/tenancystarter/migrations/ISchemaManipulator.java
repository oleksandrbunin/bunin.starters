package org.ob.starters.tenancystarter.migrations;

public interface ISchemaManipulator {

    void createSchema(String schema);

    void deleteSchema(String schema);

    void createSchema(String schema, boolean ifNotExists);

    void deleteSchema(String schema, boolean cascade, boolean ifExists);

    boolean existsSchema(String schema);
}
