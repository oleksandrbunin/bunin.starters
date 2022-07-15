package org.ob.starters;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class UtilsTestClass {

    private final ApplicationContext context;

    public UtilsTestClass(ApplicationContext context) {
        this.context = context;
    }

    public boolean isTestContext() {
        return Arrays.stream(context.getEnvironment().getActiveProfiles())
                .anyMatch(profile -> profile.contains("test"));
    }

}
