package com.fintech.finance.ledger.service;

import com.fintech.finance.ledger.TenantTestExtension;
import com.fintech.finance.ledger.common.exception.CategoryNotFoundException;
import com.fintech.finance.ledger.common.tenant.UserContext;
import com.fintech.finance.ledger.entity.Category;
import com.fintech.finance.ledger.mapper.CategoryMapper;
import com.fintech.finance.ledger.repository.CategoryRepository;
import com.model.category.CategoryDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({MockitoExtension.class, TenantTestExtension.class})
@Tag("unit")
class CategoryServiceTest {

    private static final String CATEGORY_NAME = "Food & Dining";

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    private final CategoryMapper categoryMapper = Mappers.getMapper(CategoryMapper.class);

    @BeforeEach
    void setup() {
        categoryService = new CategoryService(categoryRepository, categoryMapper);
    }

    @Test
    void createCategorySuccess() {
        var categoryDto = new CategoryDto();
        categoryDto.setName(CATEGORY_NAME);
        ArgumentCaptor<Category> categoryEntityArgumentCaptor = ArgumentCaptor.forClass(Category.class);
        categoryService.createCategory(categoryDto);
        Mockito.verify(categoryRepository, Mockito.times(1)).save(categoryEntityArgumentCaptor.capture());
        var category = categoryEntityArgumentCaptor.getValue();
        assertEquals(CATEGORY_NAME, category.getName());
    }

    @Test
    void getCategoryByIdSuccess() {
        var categoryId = UUID.randomUUID();
        var category = getCategory();
        Mockito.when(categoryRepository.findByIdAndTenantId(categoryId, UserContext.getUserContextData().tenantId())).thenReturn(Optional.of(category));
        var categoryDto = categoryService.getCategoryById(categoryId);
        assertEquals(CATEGORY_NAME, categoryDto.getName());
    }

    @Test
    void getCategoryByIdFails() {
        var categoryId = UUID.randomUUID();
        Mockito.when(categoryRepository.findByIdAndTenantId(categoryId, UserContext.getUserContextData().tenantId())).thenReturn(Optional.empty());
        assertThrows(CategoryNotFoundException.class, () -> categoryService.getCategoryById(categoryId));
    }

    @Test
    void getAllCategoriesSuccess() {
        Page<Category> page = new PageImpl<>(List.of(getCategory()), PageRequest.of(0, 10), 1);
        Mockito.when(categoryRepository.findAllByTenantId(ArgumentMatchers.eq(UserContext.getUserContextData().tenantId()), ArgumentMatchers.any())).thenReturn(page);
        var categoryPage = categoryService.getAllCategories(Mockito.mock(Pageable.class));
        Mockito.verify(categoryRepository, Mockito.times(1)).findAllByTenantId(ArgumentMatchers.eq(UserContext.getUserContextData().tenantId()), ArgumentMatchers.any());
        assertEquals(1, categoryPage.getTotalElements());
        assertEquals(1, categoryPage.getTotalPages());

        var categoryDto = categoryPage.getContent().get(0);
        assertEquals(CATEGORY_NAME, categoryDto.getName());
    }

    @Test
    void testDeleteCategoryByIdSuccess() {
        var categoryId = UUID.randomUUID();
        Mockito.when(categoryRepository.deleteByIdAndTenantId(categoryId, UserContext.getUserContextData().tenantId())).thenReturn(1);
        categoryService.deleteCategoryById(categoryId);
        Mockito.verify(categoryRepository, Mockito.times(1)).deleteByIdAndTenantId(categoryId, UserContext.getUserContextData().tenantId());
    }

    @Test
    void testDeleteCategoryByIdFails() {
        var categoryId = UUID.randomUUID();
        Mockito.when(categoryRepository.deleteByIdAndTenantId(categoryId, UserContext.getUserContextData().tenantId())).thenReturn(0);
        assertThrows(CategoryNotFoundException.class, () -> categoryService.deleteCategoryById(categoryId));
        Mockito.verify(categoryRepository, Mockito.times(1)).deleteByIdAndTenantId(categoryId, UserContext.getUserContextData().tenantId());
    }

    @Test
    void testDeleteAllCategories() {
        categoryService.deleteAllCategories();
        Mockito.verify(categoryRepository, Mockito.times(1)).deleteAllByTenantId(UserContext.getUserContextData().tenantId());
    }

    @Test
    void testUpdateCategorySuccess() {
        var categoryId = UUID.randomUUID();
        var existingCategory = getCategory();
        existingCategory.setId(categoryId);
        Mockito.when(categoryRepository.findByIdAndTenantId(categoryId, UserContext.getUserContextData().tenantId())).thenReturn(Optional.of(existingCategory));
        var updatedCategoryDto = new CategoryDto();
        updatedCategoryDto.setId(categoryId);
        updatedCategoryDto.setName("Updated Category Name");

        categoryService.updateCategory(updatedCategoryDto);

        ArgumentCaptor<Category> categoryEntityArgumentCaptor = ArgumentCaptor.forClass(Category.class);
        Mockito.verify(categoryRepository, Mockito.times(1)).save(categoryEntityArgumentCaptor.capture());
        var savedCategory = categoryEntityArgumentCaptor.getValue();
        assertEquals("Updated Category Name", savedCategory.getName());
    }

    @Test
    void testUpdateCategoryFails() {
        var categoryId = UUID.randomUUID();
        Mockito.when(categoryRepository.findByIdAndTenantId(categoryId, UserContext.getUserContextData().tenantId())).thenReturn(Optional.empty());
        var updatedCategoryDto = new CategoryDto();
        updatedCategoryDto.setId(categoryId);
        assertThrows(CategoryNotFoundException.class, () -> categoryService.updateCategory(updatedCategoryDto));
    }

    private static Category getCategory() {
        var category = new Category();
        category.setName(CATEGORY_NAME);
        return category;
    }
}

