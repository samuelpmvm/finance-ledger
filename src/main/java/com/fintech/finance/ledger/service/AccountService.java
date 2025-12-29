package com.fintech.finance.ledger.service;

import com.fintech.finance.ledger.common.exception.AccountNotFoundException;
import com.fintech.finance.ledger.common.exception.ApiErrorMessage;
import com.fintech.finance.ledger.common.tenant.UserContext;
import com.fintech.finance.ledger.mapper.AccountMapper;
import com.fintech.finance.ledger.repository.AccountRepository;
import com.model.accounts.AccountDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountService.class);

    public AccountDto createAccount (AccountDto accountDto) {
        var account = accountMapper.toEntity(accountDto);
        account.setTenantId(UserContext.getUserContextData().tenantId());
        LOGGER.info("Creating account: {}", account);
        return accountMapper.toDto(accountRepository.save(account));
    }

    public AccountDto findAccountById (UUID accountId) {
        var tenantId = UserContext.getUserContextData().tenantId();
        LOGGER.info("Fetch Account with ID: {}", accountId);
        var account = accountRepository.findByIdAndTenantId(accountId, tenantId)
                .orElseThrow(() -> new AccountNotFoundException(ApiErrorMessage.ACCOUNT_NOT_FOUND.getErrorMessage(accountId)));
        return accountMapper.toDto(account);
    }

}
