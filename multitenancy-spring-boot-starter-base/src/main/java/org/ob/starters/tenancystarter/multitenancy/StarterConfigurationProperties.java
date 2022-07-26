package org.ob.starters.tenancystarter.multitenancy;

public class StarterConfigurationProperties {

    public static final String MULTITENANCY_PROPERTY = "ob-tenancy-starter.multitenancy";

    private Multitenancy multitenancy;
    private String defaultSchema;

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
}
