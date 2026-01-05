package com.fintech.finance.ledger.service;

import com.fintech.finance.ledger.common.exception.ApiErrorMessage;
import com.fintech.finance.ledger.common.exception.CategoryNotFoundException;
import com.fintech.finance.ledger.common.tenant.UserContext;
import com.fintech.finance.ledger.common.validator.CategoryDeletionPolicy;
import com.fintech.finance.ledger.entity.Category;
import com.fintech.finance.ledger.mapper.CategoryMapper;
import com.fintech.finance.ledger.mapper.PageDtoMapper;
import com.fintech.finance.ledger.repository.CategoryRepository;
import com.model.category.CategoryDto;
import com.model.category.CategoryPage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final CategoryDeletionPolicy categoryDeletionPolicy;
    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryService.class);

    public CategoryPage getAllCategories(Pageable pageable) {
        var tenantId = UserContext.getUserContextData().tenantId();
        LOGGER.info("Fetching all categories for tenant ID: {}", tenantId);
        var categoryPage = categoryRepository.findAllByTenantId(tenantId, pageable);
        return PageDtoMapper.toCategoryPage(categoryPage.map(categoryMapper::toDto));
    }

    public CategoryPage getChildCategories(UUID parentId, Pageable pageable) {
        var tenantId = UserContext.getUserContextData().tenantId();
        LOGGER.info("Fetching child categories for parent ID: {} and tenant ID: {}", parentId, tenantId);
        var categoryPage = categoryRepository.findAllByParentIdAndTenantId(parentId, tenantId, pageable);
        return PageDtoMapper.toCategoryPage(categoryPage.map(categoryMapper::toDto));
    }

    public CategoryDto createCategory (CategoryDto categoryDto) {
        var category = categoryMapper.toEntity(categoryDto);
        category.setTenantId(UserContext.getUserContextData().tenantId());
        LOGGER.info("Creating category: {}", category);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    public CategoryDto getCategoryById(UUID categoryId) {
        var tenantId = UserContext.getUserContextData().tenantId();
        LOGGER.info("Fetch Category with ID: {}", categoryId);
        var category = categoryRepository.findByIdAndTenantId(categoryId, tenantId)
                .orElseThrow(() -> new CategoryNotFoundException(ApiErrorMessage.CATEGORY_NOT_FOUND.getErrorMessage(categoryId)));
        return categoryMapper.toDto(category);
    }

    public CategoryDto updateCategory(CategoryDto categoryDto) {
        var tenantId = UserContext.getUserContextData().tenantId();
        var categoryId = categoryDto.getId();
        LOGGER.info("Updating category with ID: {} for tenant ID: {}", categoryId, tenantId);
        var existingCategory = categoryRepository.findByIdAndTenantId(categoryId, tenantId)
                .orElseThrow(() -> new CategoryNotFoundException(ApiErrorMessage.CATEGORY_NOT_FOUND.getErrorMessage(categoryId)));
        var updatedCategory = categoryMapper.toEntity(categoryDto);
        updatedCategory.setTenantId(tenantId);
        updatedCategory.setId(existingCategory.getId());
        return categoryMapper.toDto(categoryRepository.save(updatedCategory));
    }

    public void deleteAllCategories() {
        var tenantId = UserContext.getUserContextData().tenantId();
        var categories = categoryRepository.getAllByTenantId(tenantId);
        for (Category category : categories) {
            categoryDeletionPolicy.validateCategoryDeletion(tenantId, category.getId());
        }
        LOGGER.info("Deleting all categories for tenant ID: {}", tenantId);
        categoryRepository.deleteAllByTenantId(tenantId);
    }

    public void deleteCategoryById(UUID categoryId) {
        var tenantId = UserContext.getUserContextData().tenantId();
        categoryDeletionPolicy.validateCategoryDeletion(tenantId, categoryId);
        LOGGER.info("Deleting category with ID: {} for tenant ID: {}", categoryId, tenantId);
        int deletedCount = categoryRepository.deleteByIdAndTenantId(categoryId, tenantId);
        if (deletedCount == 0) {
            throw new CategoryNotFoundException(ApiErrorMessage.CATEGORY_NOT_FOUND.getErrorMessage(categoryId));
        }
    }
}
