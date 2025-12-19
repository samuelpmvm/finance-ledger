package com.fintech.finance.ledger.repository;

import com.fintech.finance.ledger.entity.Tenant;
import com.fintech.finance.ledger.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
}
