package com.fintech.finance.ledger.common.tenant;

import java.util.UUID;

public class TenantContext {
    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setUserId(UUID tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static UUID getUserId() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
