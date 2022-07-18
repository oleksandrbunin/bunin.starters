--liquibase formatted sql

--changeset bunin-o:test-migration splitStatements:false logicalFilePath:classpath:/sample-tenant-migrations/test-migration.sql
CREATE TABLE "${database.defaultSchemaName}".test (
    id varchar unique primary key
);

INSERT INTO "${database.defaultSchemaName}".test (id) VALUES ('TEST')
