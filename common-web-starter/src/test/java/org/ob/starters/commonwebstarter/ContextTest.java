package org.ob.starters.commonwebstarter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ob.starters.commonwebstarter.configuration.TestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.UriComponentsBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

@SpringBootTest(classes = {
        TestConfiguration.class,
        CommonWebStarterAutoConfiguration.class
})
@SpringJUnitWebConfig
@Tag("integration")
@AutoConfigureMockMvc
@ActiveProfiles({"test"})
@TestPropertySource(locations = "classpath:/application-test.yaml")
class ContextTest {

    private static final String TENANT_X = "TENANT-X";

    protected MockMvc mockMvc;

    @Autowired
    protected WebApplicationContext webApplicationContext;

    protected HttpHeaders commonHeaders;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .alwaysDo(MockMvcResultHandlers.print())
                .build();
    }

    @Test
    void dummyTest() {
        assertThat(1 + 1).isEqualTo(2);
    }

    @Test
    void tenantInterceptorTest() throws Exception {
        commonHeaders = new HttpHeaders();
        commonHeaders.set(TenantContextInterceptor.TENANT_HEADER_NAME, TENANT_X);

        final UriComponentsBuilder uriBuilder = fromUriString("/api/v1/echo-tenant");

        final MvcResult resultData = mockMvc.perform(
                get(uriBuilder.build().encode().toUri())
                        .headers(commonHeaders)
                        .accept(MediaType.TEXT_PLAIN_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(resultData.getResponse().getContentAsString()).isEqualTo(TENANT_X);
    }

}
