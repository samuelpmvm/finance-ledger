package com.fintech.finance.ledger.common.tenant;

public final class UserContext {
    private static final ThreadLocal<UserContextData> CURRENT_CONTEXT = new ThreadLocal<>();

    private UserContext() {
    }

    public static void setUserContextData(UserContextData userContextData) {
        CURRENT_CONTEXT.set(userContextData);
    }

    public static UserContextData getUserContextData() {
        return CURRENT_CONTEXT.get();
    }

    public static void clear() {
        CURRENT_CONTEXT.remove();
    }
}
