package com.fintech.finance.ledger.repository;

import com.fintech.finance.ledger.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends TenantAwareRepository<Category, UUID> {

    @Override
    Page<Category> findAllByTenantId(UUID tenantId, Pageable pageable);

    Optional<Category> findByIdAndTenantId(UUID categoryId, UUID tenantId);

    Page<Category> findAllByParentIdAndTenantId(UUID parentId, UUID tenantId, Pageable pageable);

    @Modifying
    @Transactional
    void deleteAllByTenantId(UUID tenantId);

    @Modifying
    @Transactional
    int deleteByIdAndTenantId(UUID categoryId,UUID tenantId);

    boolean existsByTenantIdAndParentId(UUID tenantId, UUID parentId);

    List<Category> getAllByTenantId(UUID tenantId);
}
