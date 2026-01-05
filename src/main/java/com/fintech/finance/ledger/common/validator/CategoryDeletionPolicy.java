package com.fintech.finance.ledger.common.validator;

import com.fintech.finance.ledger.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryDeletionPolicy {

    private final CategoryRepository categoryRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryDeletionPolicy.class);

    public void validateCategoryDeletion(java.util.UUID tenantId, java.util.UUID categoryId) {
        LOGGER.info("Validating deletion policy for category ID: {} under tenant ID: {}", categoryId, tenantId);
        boolean hasChildCategories = categoryRepository.existsByTenantIdAndParentId(tenantId, categoryId);
        if (hasChildCategories) {
            throw new com.fintech.finance.ledger.common.exception.CategoryDeletionNotAllowedException("Category with ID: " + categoryId + " cannot be deleted as it has associated child categories.");
        }
    }

}
