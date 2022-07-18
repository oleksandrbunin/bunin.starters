package org.ob.starters.tenancystarter;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
class ContextTest extends BaseTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void trueTest() {
        assertThat(1 + 1).isEqualTo(2);
    }

    @Test
    void propertySourceTest() {
        assertThat(
                applicationContext.getEnvironment().getProperty("property.source.value")
        ).isEqualTo("TestPropertySource");
    }


}
