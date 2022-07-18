--liquibase formatted sql

--changeset bunin-o:test-migration splitStatements:false logicalFilePath:classpath:/sample-default-migrations/test-migration.sql
CREATE TABLE test (
    id uuid unique primary key
)
