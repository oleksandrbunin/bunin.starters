package org.ob.starters.tenancystarter.migrations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class MigrationPathsProvider implements IMigrationPathProvider {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String DEFAULT_CHANGELOG = "classpath*:**/default_schema-changelog.xml";

    protected static final String TENANTS_CHANGELOG = "classpath*:**/tenants-changelog.xml";

    private static final String TEST_MIGRATION_PATH = "/target/test-classes/";

    private final ResourcePatternResolver resourceResolver;

    private final Map<String, Resource[]> resourceScan = new ConcurrentHashMap<>();

    public MigrationPathsProvider(ResourcePatternResolver resourceResolver) {
        this.resourceResolver = resourceResolver;
    }

    @Override
    public List<URI> tenantsMigrationsPaths() {
        return migrationPaths(TENANTS_CHANGELOG);
    }

    @Override
    public List<URI> defaultMigrationsPaths() {
        return migrationPaths(DEFAULT_CHANGELOG);
    }

    private List<URI> migrationPaths(String pattern) {
        Resource[] liquibaseMigrationPaths = resourceScan.computeIfAbsent(pattern, this::migrationResources);
        List<URI> resourcesURIs = toURIs(liquibaseMigrationPaths);
        logger.info("Found resources that match pattern {}: {}", 
                pattern, resourcesURIs
        );
        return resourcesURIs.stream().sorted(new MigrationsResourcesComparator()).toList();
    }

    private Resource[] migrationResources(String pattern) {
        try {
            return resourceResolver.getResources(pattern);
        } catch (IOException e) {
            throw new RuntimeException("Migrations cannot be resolved for " + pattern, e);
        }
    }

    private List<URI> toURIs(Resource[] migrationsPaths) {
        return Stream.of(migrationsPaths)
                .map(this::getUri)
                .toList();
    }

    private URI getUri(Resource resource) {
        try {
            return resource.getURI();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final class MigrationsResourcesComparator implements Comparator<URI> {

        @Override
        public int compare(URI u1, URI u2) {
            String migrationPath1 = u1.toString();
            String migrationPath2 = u2.toString();
            // test-migrations must be the last migrations
            // starters and application migrations should be independent
            return compareRegardingTestMigrations(migrationPath1, migrationPath2);
        }

        private int compareRegardingTestMigrations(String migrationPath1, String migrationPath2) {
             if (!migrationPath1.contains(TEST_MIGRATION_PATH)
                    && migrationPath2.contains(TEST_MIGRATION_PATH)) {
                return -1;
            } else if (migrationPath1.contains(TEST_MIGRATION_PATH)
                    && !migrationPath2.contains(TEST_MIGRATION_PATH)) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
