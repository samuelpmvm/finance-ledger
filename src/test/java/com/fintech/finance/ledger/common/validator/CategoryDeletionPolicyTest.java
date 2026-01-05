package com.fintech.finance.ledger.common.validator;

import com.fintech.finance.ledger.common.exception.CategoryDeletionNotAllowedException;
import com.fintech.finance.ledger.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class CategoryDeletionPolicyTest {

    @InjectMocks
    private CategoryDeletionPolicy categoryDeletionPolicy;

    @Mock
    private CategoryRepository categoryRepository;

    private UUID tenantId;
    private UUID categoryId;

    @BeforeEach
    void setup() {
        tenantId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
    }

    @Test
    void validateCategoryDeletion_ShouldPass_WhenCategoryHasNoChildCategories() {
        Mockito.when(categoryRepository.existsByTenantIdAndParentId(tenantId, categoryId))
                .thenReturn(false);

        assertDoesNotThrow(() -> categoryDeletionPolicy.validateCategoryDeletion(tenantId, categoryId));

        Mockito.verify(categoryRepository, Mockito.times(1))
                .existsByTenantIdAndParentId(tenantId, categoryId);
    }

    @Test
    void validateCategoryDeletion_ShouldThrowException_WhenCategoryHasChildCategories() {
        Mockito.when(categoryRepository.existsByTenantIdAndParentId(tenantId, categoryId))
                .thenReturn(true);

        CategoryDeletionNotAllowedException exception = assertThrows(
                CategoryDeletionNotAllowedException.class,
                () -> categoryDeletionPolicy.validateCategoryDeletion(tenantId, categoryId)
        );

        assertTrue(exception.getMessage().contains(categoryId.toString()));
        assertTrue(exception.getMessage().contains("cannot be deleted"));
        assertTrue(exception.getMessage().contains("child categories"));
        Mockito.verify(categoryRepository, Mockito.times(1))
                .existsByTenantIdAndParentId(tenantId, categoryId);
    }
}

