package com.fintech.finance.ledger.mapper;

import com.fintech.finance.ledger.entity.Category;
import com.model.category.CategoryDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDto toDto(Category category);

    Category toEntity(CategoryDto dto);
}
