package com.fintech.finance.ledger.repository;

import com.fintech.finance.ledger.common.tenant.UserContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.UUID;

@NoRepositoryBean
public interface TenantAwareRepository<T, ID>
        extends JpaRepository<T, ID> {

    default UUID tenantId() {;
        return UserContext.getUserContextData().tenantId();
    }
}

