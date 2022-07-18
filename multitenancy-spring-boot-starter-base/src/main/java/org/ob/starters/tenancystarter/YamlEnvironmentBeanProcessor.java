package org.ob.starters.tenancystarter;

import org.ob.starters.tenancystarter.exceptions.IOYamlException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class YamlEnvironmentBeanProcessor implements EnvironmentPostProcessor {

    private static final YamlPropertySourceLoader yamlLoader = new YamlPropertySourceLoader();
    private static final String PROPERTIES_NAME = "ob-multitenancy-starter";
    private static final String PROPERTIES_SOURCE = PROPERTIES_NAME + ".yaml";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment,
                                       SpringApplication application) {
        try {
            environment.getPropertySources()
                    .addLast(
                            yamlLoader.load(PROPERTIES_NAME, new ClassPathResource(PROPERTIES_SOURCE)).get(0)
                    );
        } catch (IOException e) {
            throw new IOYamlException(
                    "IOException while loading %s yaml in the ClassPathResource %s ".formatted(
                            PROPERTIES_NAME, PROPERTIES_SOURCE
                    ), e
            );
        }
    }
}
