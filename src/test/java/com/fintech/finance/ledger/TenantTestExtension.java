package com.fintech.finance.ledger;

import com.fintech.finance.ledger.common.tenant.UserContext;
import com.fintech.finance.ledger.common.tenant.UserContextData;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.UUID;

public class TenantTestExtension
        implements BeforeEachCallback, AfterEachCallback {

    private static final UUID TEST_TENANT_ID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID TEST_USER_ID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String USER_AUTH = "user";

    @Override
    public void beforeEach(ExtensionContext context) {
        var userContextData = new UserContextData(TEST_USER_ID, TEST_TENANT_ID, USER_AUTH);
        UserContext.setUserContextData(userContextData);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        UserContext.clear();
    }
}

