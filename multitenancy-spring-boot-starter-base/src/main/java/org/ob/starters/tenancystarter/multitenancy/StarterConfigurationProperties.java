package org.ob.starters.tenancystarter.multitenancy;

import java.util.concurrent.TimeUnit;

public class StarterConfigurationProperties {

    public static final String MULTITENANCY_PROPERTY = "ob-tenancy-starter.multitenancy";

    private Multitenancy multitenancy;
    private String defaultSchema;
    private SchemaLockTimeout schemaLockTimeout;

    public Multitenancy getMultitenancy() {
        return multitenancy;
    }

    public void setMultitenancy(Multitenancy multitenancy) {
        this.multitenancy = multitenancy;
    }

    public String getDefaultSchema() {
        return defaultSchema;
    }

    public void setDefaultSchema(String defaultSchema) {
        this.defaultSchema = defaultSchema;
    }

    public SchemaLockTimeout getSchemaLockTimeout() {
        return schemaLockTimeout;
    }

    public void setSchemaLockTimeout(SchemaLockTimeout schemaLockTimeout) {
        this.schemaLockTimeout = schemaLockTimeout;
    }

    public static class SchemaLockTimeout {

        private TimeUnit timeUnit;
        private long timeout;

        public TimeUnit getTimeUnit() {
            return timeUnit;
        }

        public void setTimeUnit(TimeUnit timeUnit) {
            this.timeUnit = timeUnit;
        }

        public long getTimeout() {
            return timeout;
        }

        public void setTimeout(long timeout) {
            this.timeout = timeout;
        }
    }
}
