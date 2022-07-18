package org.ob.starters.tenancystarter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.ob.starters.tenancystarter.multitenancy.StarterConfigurationProperties;
import org.ob.starters.tenancystarter.multitenancy.CustomLiquibaseProperties;

@ConditionalOnProperty(value = "ob-multitenancy-starter.enabled",
		havingValue = "true",
		matchIfMissing = true)
public class MultitenancyStarterConfiguration {

	@Bean("defaultLiquibaseProperties")
	@ConfigurationProperties(prefix = "ob-multitenancy-starter.default-liquibase")
	public CustomLiquibaseProperties defaultLiquibaseProperties() {
		return new CustomLiquibaseProperties();
	}

	@Bean
	@ConfigurationProperties(prefix = "ob-multitenancy-starter")
	public StarterConfigurationProperties starterConfigurationProperties() {
		return new StarterConfigurationProperties();
	}

}
