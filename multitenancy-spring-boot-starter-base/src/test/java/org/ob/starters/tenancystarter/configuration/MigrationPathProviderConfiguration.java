package org.ob.starters.tenancystarter.configuration;

import org.ob.starters.tenancystarter.migrations.IMigrationPathProvider;
import org.ob.starters.tenancystarter.migrations.MigrationPathsProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@Configuration
@org.springframework.boot.test.context.TestConfiguration
public class MigrationPathProviderConfiguration {

    @Bean
    public IMigrationPathProvider migrationPathProvider() {
        return new MigrationPathsProvider(new PathMatchingResourcePatternResolver());
    }

}
