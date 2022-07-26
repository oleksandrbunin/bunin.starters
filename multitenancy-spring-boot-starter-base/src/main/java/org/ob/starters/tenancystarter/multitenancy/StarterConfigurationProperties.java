package org.ob.starters.tenancystarter.multitenancy;

import java.util.concurrent.TimeUnit;

public class StarterConfigurationProperties {

    public static final String MULTITENANCY_PROPERTY = "ob-multitenancy-starter.multitenancy";

    private Multitenancy multitenancy;
    private String defaultSchema;
    private CacheConfigurationProperties cache;
    private PackageScanProperties defaultScan;
    private PackageScanProperties tenantScan;

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

    public PackageScanProperties getDefaultScan() {
        return defaultScan;
    }

    public void setDefaultScan(PackageScanProperties defaultScan) {
        this.defaultScan = defaultScan;
    }

    public CacheConfigurationProperties getCache() {
        return cache;
    }

    public void setCache(CacheConfigurationProperties cache) {
        this.cache = cache;
    }

    public PackageScanProperties getTenantScan() {
        return tenantScan;
    }

    public void setTenantScan(PackageScanProperties tenantScan) {
        this.tenantScan = tenantScan;
    }

    public static final class PackageScanProperties {

        private String[] entityPackages;

        public String[] getEntityPackages() {
            return entityPackages;
        }

        public void setEntityPackages(String[] entityPackages) {
            this.entityPackages = entityPackages;
        }
    }

    public static class CacheConfigurationProperties {

        private Integer maxSize;
        private Integer expiration;
        private TimeUnit timeUnit;

        public TimeUnit getTimeUnit() {
            return timeUnit;
        }

        public void setTimeUnit(TimeUnit timeUnit) {
            this.timeUnit = timeUnit;
        }

        public Integer getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(Integer maxSize) {
            this.maxSize = maxSize;
        }

        public Integer getExpiration() {
            return expiration;
        }

        public void setExpiration(Integer expiration) {
            this.expiration = expiration;
        }

    }
}
