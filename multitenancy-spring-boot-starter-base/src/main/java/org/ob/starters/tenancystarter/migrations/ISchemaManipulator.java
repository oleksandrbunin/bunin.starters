package org.ob.starters.tenancystarter.migrations;

public interface ISchemaManipulator {
    void createSchema(String schema);

    void deleteSchema(String schema);

    boolean existsSchema(String schema);
}
