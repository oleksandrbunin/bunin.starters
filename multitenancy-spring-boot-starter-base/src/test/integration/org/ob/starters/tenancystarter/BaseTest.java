package org.ob.starters.tenancystarter;

import org.ob.starters.tenancystarter.configuration.EmptyConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;

@SpringBootTest(classes = {
        EmptyConfiguration.class,
        TenancyStarterConfiguration.class,
        YamlEnvironmentBeanProcessor.class
})
@SpringJUnitWebConfig
@AutoConfigureMockMvc
@ActiveProfiles({"test"})
@TestPropertySource(locations = "classpath:/application-test.yaml")
abstract class BaseTest {


}
