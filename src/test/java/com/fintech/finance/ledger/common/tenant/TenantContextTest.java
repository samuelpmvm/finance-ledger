package com.fintech.finance.ledger.common.tenant;

import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TenantContextTest {

    public static final UUID USER_ID = UUID.randomUUID();

    @Test
    void validateSetUserIdAndClear() {

        TenantContext.setUserId(USER_ID);
        assertEquals(USER_ID, TenantContext.getUserId());

        TenantContext.clear();
        assertNull(TenantContext.getUserId());


    }

}