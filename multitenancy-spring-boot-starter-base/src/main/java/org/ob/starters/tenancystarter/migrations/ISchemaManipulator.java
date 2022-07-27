package org.ob.starters.tenancystarter.migrations;

import java.sql.SQLException;

public interface ISchemaManipulator {

    void createSchema(String schema) throws SQLException;

    void deleteSchema(String schema) throws SQLException;

    void createSchema(String schema, boolean ifNotExists) throws SQLException;

    void deleteSchema(String schema, boolean cascade, boolean ifExists) throws SQLException;

    boolean existsSchema(String schema) throws SQLException;
}
