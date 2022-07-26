package org.ob.starters.tenancystarter;

import org.ob.starters.tenancystarter.multitenancy.CustomLiquibaseProperties;
import org.ob.starters.tenancystarter.multitenancy.StarterConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

@ConditionalOnProperty(value = "ob-tenancy-starter.enabled",
		havingValue = "true",
		matchIfMissing = true)
public class TenancyStarterConfiguration {

	@Bean("defaultLiquibaseProperties")
	@ConfigurationProperties(prefix = "ob-tenancy-starter.default-liquibase")
	public CustomLiquibaseProperties defaultLiquibaseProperties() {
		return new CustomLiquibaseProperties();
	}

	@Bean("tenancyLiquibaseProperties")
	@ConfigurationProperties(prefix = "ob-tenancy-starter.multitenancy-liquibase")
	public CustomLiquibaseProperties tenancyLiquibaseProperties() {
		return new CustomLiquibaseProperties();
	}

	@Bean
	@ConfigurationProperties(prefix = "ob-tenancy-starter")
	public StarterConfigurationProperties starterConfigurationProperties() {
		return new StarterConfigurationProperties();
	}

}
