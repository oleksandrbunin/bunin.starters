package integration.org.ob.starters.tenancystarter;

import integration.org.ob.starters.tenancystarter.multitenancy.CustomLiquibaseProperties;
import integration.org.ob.starters.tenancystarter.multitenancy.StarterConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

@ConditionalOnProperty(value = "ob-multitenancy-starter.enabled",
		havingValue = "true",
		matchIfMissing = true)
public class MultitenancyStarterConfiguration {

	@Bean("defaultLiquibaseProperties")
	@ConfigurationProperties(prefix = "ob-multitenancy-starter.default-liquibase")
	public CustomLiquibaseProperties defaultLiquibaseProperties() {
		return new CustomLiquibaseProperties();
	}

	@Bean("tenancyLiquibaseProperties")
	@ConfigurationProperties(prefix = "ob-multitenancy-starter.multitenancy-liquibase")
	public CustomLiquibaseProperties tenancyLiquibaseProperties() {
		return new CustomLiquibaseProperties();
	}

	@Bean
	@ConfigurationProperties(prefix = "ob-multitenancy-starter")
	public StarterConfigurationProperties starterConfigurationProperties() {
		return new StarterConfigurationProperties();
	}

}
