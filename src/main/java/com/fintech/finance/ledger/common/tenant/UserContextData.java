package com.fintech.finance.ledger.common.tenant;

import java.util.UUID;

public record UserContextData(
        UUID userId,
        UUID tenantId,
        String authProviderUserId
) {}

