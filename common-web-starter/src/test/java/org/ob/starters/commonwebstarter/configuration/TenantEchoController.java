package org.ob.starters.commonwebstarter.configuration;

import io.swagger.annotations.ApiOperation;
import org.ob.starters.commonwebstarter.TenantContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TenantEchoController {

    private static final String TENANT_ECHO_API = "/api/v1/echo-tenant";

    @GetMapping(value = TENANT_ECHO_API, produces = MediaType.TEXT_PLAIN_VALUE)
    @ApiOperation(
            value = "echo tenant endpoint",
            httpMethod = "GET",
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    @ResponseBody
    public String echoTenant() {
        return TenantContext.getTenantContext();
    }

}
