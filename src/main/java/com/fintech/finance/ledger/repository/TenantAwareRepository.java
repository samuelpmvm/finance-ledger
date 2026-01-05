package com.fintech.finance.ledger.repository;

import com.fintech.finance.ledger.common.tenant.UserContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.UUID;

@NoRepositoryBean
public interface TenantAwareRepository<T, I>
        extends JpaRepository<T, I> {

    default UUID tenantId() {
        return UserContext.getUserContextData().tenantId();
    }

    Page<T> findAllByTenantId(UUID tenantId, Pageable pageable);
}

