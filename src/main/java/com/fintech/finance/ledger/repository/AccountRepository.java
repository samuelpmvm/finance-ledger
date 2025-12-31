package com.fintech.finance.ledger.repository;

import com.fintech.finance.ledger.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends TenantAwareRepository<Account, UUID> {

    Page<Account> findAllByTenantId(UUID tenantId, Pageable pageable);

    Optional<Account> findByIdAndTenantId(UUID accountId, UUID tenantId);

    @Modifying
    @Transactional
    void deleteAllByTenantId(UUID tenantId);

    @Modifying
    @Transactional
    int deleteByIdAndTenantId(UUID accountId,UUID tenantId);
}
