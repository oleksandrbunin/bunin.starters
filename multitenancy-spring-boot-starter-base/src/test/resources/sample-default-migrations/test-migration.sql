--liquibase formatted sql

--changeset bunin-o:test-migration splitStatements:false logicalFilePath:classpath:/sample-default-migrations/test-migration.sql
CREATE TABLE "${database.defaultSchemaName}".defaultTestTable (
    id varchar unique primary key
);

INSERT INTO "${database.defaultSchemaName}".defaultTestTable (id) VALUES ('TEST')
