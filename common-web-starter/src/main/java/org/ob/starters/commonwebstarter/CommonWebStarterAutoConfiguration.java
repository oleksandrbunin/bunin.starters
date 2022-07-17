package org.ob.starters.commonwebstarter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Objects;

@Configuration
public class CommonWebStarterAutoConfiguration implements WebMvcConfigurer {

    private final ITenantIDResolver tenantIDResolver;

    public CommonWebStarterAutoConfiguration(@Autowired(required = false) ITenantIDResolver tenantIDResolver) {
        this.tenantIDResolver = Objects.requireNonNullElse(tenantIDResolver, (tenantId -> tenantId));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TenantContextInterceptor(tenantIDResolver));
    }


}
