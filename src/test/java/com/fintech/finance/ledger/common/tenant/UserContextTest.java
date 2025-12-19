package com.fintech.finance.ledger.common.tenant;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserContextTest {

    public static final UUID USER_ID = UUID.randomUUID();

    @Test
    void validateSetUserContextDataAndClear() {

        UserContext.setUserContextData(new UserContextData(USER_ID, UUID.randomUUID(), "provider"));
        assertEquals(USER_ID, UserContext.getUserContextData().userId());

        UserContext.clear();
        assertNull(UserContext.getUserContextData());


    }

}