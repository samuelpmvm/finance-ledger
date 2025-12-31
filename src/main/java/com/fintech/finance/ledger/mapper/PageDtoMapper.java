package com.fintech.finance.ledger.mapper;

import com.model.accounts.AccountDto;
import com.model.accounts.AccountPage;
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
}
