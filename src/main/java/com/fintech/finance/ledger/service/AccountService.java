package com.fintech.finance.ledger.service;

import com.fintech.finance.ledger.common.exception.AccountNotFoundException;
import com.fintech.finance.ledger.common.exception.ApiErrorMessage;
import com.fintech.finance.ledger.common.tenant.UserContext;
import com.fintech.finance.ledger.mapper.AccountMapper;
import com.fintech.finance.ledger.mapper.PageDtoMapper;
import com.fintech.finance.ledger.repository.AccountRepository;
import com.model.accounts.AccountDto;
import com.model.accounts.AccountPage;
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

    public AccountDto getAccountById(UUID accountId) {
        var tenantId = UserContext.getUserContextData().tenantId();
        LOGGER.info("Fetch Account with ID: {}", accountId);
        var account = accountRepository.findByIdAndTenantId(accountId, tenantId)
                .orElseThrow(() -> new AccountNotFoundException(ApiErrorMessage.ACCOUNT_NOT_FOUND.getErrorMessage(accountId)));
        return accountMapper.toDto(account);
    }

    public AccountPage getAllAccounts(Pageable pageable) {
        var tenantId = UserContext.getUserContextData().tenantId();
        LOGGER.info("Fetching all accounts for tenant ID: {}", tenantId);
        var accountPage = accountRepository.findAllByTenantId(tenantId, pageable);
        return PageDtoMapper.toAccountPage(accountPage.map(accountMapper::toDto));
    }

    public void deleteAllAccounts() {
        var tenantId = UserContext.getUserContextData().tenantId();
        LOGGER.info("Deleting all accounts for tenant ID: {}", tenantId);
        accountRepository.deleteAllByTenantId(tenantId);
    }

    public void deleteAccountById(UUID accountId) {
        var tenantId = UserContext.getUserContextData().tenantId();
        LOGGER.info("Deleting account with ID: {} for tenant ID: {}", accountId, tenantId);
        int deletedCount = accountRepository.deleteByIdAndTenantId(accountId, tenantId);
        if (deletedCount == 0) {
            throw new AccountNotFoundException(ApiErrorMessage.ACCOUNT_NOT_FOUND.getErrorMessage(accountId));
        }
    }

    public AccountDto updateAccount(AccountDto accountDto) {
        var tenantId = UserContext.getUserContextData().tenantId();
        var accountId = accountDto.getId();
        LOGGER.info("Updating account with ID: {} for tenant ID: {}", accountId, tenantId);
        var existingAccount = accountRepository.findByIdAndTenantId(accountId, tenantId)
                .orElseThrow(() -> new AccountNotFoundException(ApiErrorMessage.ACCOUNT_NOT_FOUND.getErrorMessage(accountId)));
        var updatedAccount = accountMapper.toEntity(accountDto);
        updatedAccount.setTenantId(tenantId);
        updatedAccount.setId(existingAccount.getId());
        return accountMapper.toDto(accountRepository.save(updatedAccount));
    }

}
