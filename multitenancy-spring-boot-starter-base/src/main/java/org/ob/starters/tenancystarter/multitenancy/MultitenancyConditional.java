package org.ob.starters.tenancystarter.multitenancy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.upperCase;
import static org.ob.starters.tenancystarter.multitenancy.StarterConfigurationProperties.MULTITENANCY_PROPERTY;

public abstract class MultitenancyConditional implements Condition {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        final String value = upperCase(
                Objects.toString(context.getEnvironment().getProperty(MULTITENANCY_PROPERTY),
                        Multitenancy.SCHEMA_PER_TENANT.toString())
        );
        try {
            return Multitenancy.valueOf(value) == getMultitenancy();
        } catch (IllegalArgumentException exception) {
            logger.error(
                    "Incorrect value passed to {} property, was {}, but expected: {}",
                    MULTITENANCY_PROPERTY, value,
                    List.of(Multitenancy.SCHEMA_PER_TENANT, Multitenancy.SCHEMA_PER_TENANT, Multitenancy.NONE)
            );
            throw exception;
        }
    }

    protected abstract Multitenancy getMultitenancy();

}
