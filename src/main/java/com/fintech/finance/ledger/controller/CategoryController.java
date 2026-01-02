package com.fintech.finance.ledger.controller;

import com.api.category.CategoryControllerApi;
import com.fintech.finance.ledger.service.CategoryService;
import com.model.category.CategoryDto;
import com.model.category.CategoryPage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CategoryController implements CategoryControllerApi {

    private final CategoryService categoryService;

    @Override
    public ResponseEntity<CategoryDto> createCategory(CategoryDto categoryDto) {
        var createdCategory = categoryService.createCategory(categoryDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    @Override
    public ResponseEntity<Void> deleteCategoryById(UUID categoryId) {
        categoryService.deleteCategoryById(categoryId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> deleteAllCategories() {
        categoryService.deleteAllCategories();
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<CategoryDto> getCategoryById(UUID categoryId) {
        var categoryDto = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(categoryDto);
    }

    @Override
    public ResponseEntity<CategoryPage> getAllCategories(Integer page, Integer size) {
        var pageNumber = page != null ? page : 0;
        var pageSize = size != null ? size : 10;
        var pageRequest = PageRequest.of(pageNumber, pageSize);
        return ResponseEntity.ok(categoryService.getAllCategories(pageRequest));
    }

    @Override
    public ResponseEntity<CategoryDto> updateCategory(CategoryDto categoryDto) {
        var updatedCategory = categoryService.updateCategory(categoryDto);
        return ResponseEntity.ok(updatedCategory);
    }
}

