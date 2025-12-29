package com.fintech.finance.ledger.repository;

import com.fintech.finance.ledger.entity.Account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends TenantAwareRepository<Account, UUID> {

    List<Account> findAllByTenantId(UUID tenantId);

    Optional<Account> findByIdAndTenantId(UUID accountId, UUID tenantId);
}
