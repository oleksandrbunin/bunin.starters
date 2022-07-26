package org.ob.starters.tenancystarter.migrations;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Objects;

public class SchemaManipulator implements ISchemaManipulator {

    private final JdbcTemplate jdbcTemplate;

    public SchemaManipulator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public void createSchema(String schema) {
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS \"%s\"".formatted(schema));
    }

    @Override
    public void deleteSchema(String schema) {
        jdbcTemplate.execute("DROP SCHEMA IF EXISTS \"%s\" CASCADE".formatted(schema));
    }

    @Override
    public boolean existsSchema(String schema) {
        // can be null
        return "public".equals(schema) ||
                Objects.equals(
                        jdbcTemplate.queryForObject(
                                "SELECT EXISTS(SELECT 1 FROM pg_namespace WHERE nspname = '%s')".formatted(schema),
                                Boolean.class
                        ), Boolean.TRUE
                );
    }
}
