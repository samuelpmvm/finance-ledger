package com.fintech.finance.ledger.mapper;

import com.fintech.finance.ledger.entity.Account;
import com.model.accounts.AccountDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    AccountDto toDto(Account account);

    Account toEntity(AccountDto dto);
}