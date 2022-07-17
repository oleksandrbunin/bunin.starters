package org.ob.starters.commonwebstarter;

public class TenantContext {

    private static final ThreadLocal<String> TENANT_CONTEXT = new ThreadLocal<>();

    public static void setTenantContext(String tenant) {
        TENANT_CONTEXT.set(tenant);
    }

    public static String getTenantContext() {
        return TENANT_CONTEXT.get();
    }

    public static void clearTenantContext() {
        TENANT_CONTEXT.remove();
    }


}
