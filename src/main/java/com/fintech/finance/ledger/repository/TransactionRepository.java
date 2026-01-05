package com.fintech.finance.ledger.repository;

import com.fintech.finance.ledger.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends TenantAwareRepository<Transaction, UUID> {

    @Override
    Page<Transaction> findAllByTenantId(UUID tenantId, Pageable pageable);

    Optional<Transaction> findByIdAndTenantId(UUID transactionId, UUID tenantId);

    Page<Transaction> findAllByAccountIdAndTenantId(UUID accountId, UUID tenantId, Pageable pageable);

    Page<Transaction> findAllByCategoryIdAndTenantId(UUID categoryId, UUID tenantId, Pageable pageable);

    @Modifying
    @Transactional
    void deleteAllByTenantId(UUID tenantId);

    @Modifying
    @Transactional
    int deleteByIdAndTenantId(UUID transactionId, UUID tenantId);

    boolean existsByTenantIdAndAccountId(UUID tenantId, UUID accountId);
}

