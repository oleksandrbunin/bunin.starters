package org.ob.starters.tenancystarter;

import org.junit.jupiter.api.Tag;
import org.ob.starters.tenancystarter.configuration.EmptyConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;

@SpringBootTest(classes = {
        EmptyConfiguration.class,
        MultitenancyStarterConfiguration.class,
        YamlEnvironmentBeanProcessor.class
})
@SpringJUnitWebConfig
@AutoConfigureMockMvc
@ActiveProfiles({"test"})
@TestPropertySource(locations = "classpath:/application-test.yaml")
@Tag("integration")
abstract class BaseTest {


}
