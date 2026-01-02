package com.fintech.finance.ledger.mapper;

import com.model.accounts.AccountDto;
import com.model.accounts.AccountPage;
import com.model.category.CategoryDto;
import com.model.category.CategoryPage;
import org.springframework.data.domain.Page;

public final class PageDtoMapper {

    private PageDtoMapper() {
    }

    public static AccountPage toAccountPage(Page<AccountDto> accountDtoPage) {
        var accountPage = new AccountPage();
        accountPage.setContent(accountDtoPage.getContent());
        accountPage.setPage(accountDtoPage.getNumber());
        accountPage.setSize(accountDtoPage.getSize());
        accountPage.setTotalElements(accountDtoPage.getTotalElements());
        accountPage.setTotalPages(accountDtoPage.getTotalPages());
        accountPage.setLast(accountDtoPage.isLast());
        return accountPage;
    }

    public static CategoryPage toCategoryPage(Page<CategoryDto> categoryDtoPage) {
        var categoryPage = new CategoryPage();
        categoryPage.setContent(categoryDtoPage.getContent());
        categoryPage.setSize(categoryDtoPage.getSize());
        categoryPage.setPage(categoryDtoPage.getNumber());
        categoryPage.setTotalElements(categoryDtoPage.getTotalElements());
        categoryPage.setTotalPages(categoryDtoPage.getTotalPages());
        categoryPage.setLast(categoryDtoPage.isLast());
        return categoryPage;
    }
}
