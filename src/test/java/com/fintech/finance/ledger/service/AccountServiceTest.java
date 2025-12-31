package com.fintech.finance.ledger.service;

import com.fintech.finance.ledger.TenantTestExtension;
import com.fintech.finance.ledger.common.exception.AccountNotFoundException;
import com.fintech.finance.ledger.common.tenant.UserContext;
import com.fintech.finance.ledger.entity.Account;
import com.fintech.finance.ledger.mapper.AccountMapper;
import com.fintech.finance.ledger.repository.AccountRepository;
import com.model.accounts.AccountDto;
import com.model.accounts.AccountType;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({MockitoExtension.class, TenantTestExtension.class})
@Tag("unit")
class AccountServiceTest {

    private static final String ACCOUNT_NAME = "account";
    private static final Double BALANCE = 0.0;
    private static final AccountType ACCOUNT_TYPE = AccountType.BANK;
    private static final boolean INCLUDE_IN_BUDGET = true;

    @InjectMocks
    private AccountService accountService;

    @Mock
    private AccountRepository accountRepository;

    private final AccountMapper accountMapper = Mappers.getMapper(AccountMapper.class);

    @BeforeEach
    void setup() {
        accountService = new AccountService(accountRepository, accountMapper);
    }

    @Test
    void createAccountSuccess() {
        var accountDto = new AccountDto(ACCOUNT_NAME, BALANCE, ACCOUNT_TYPE, INCLUDE_IN_BUDGET);
        ArgumentCaptor<Account> accountEntityArgumentCaptor = ArgumentCaptor.forClass(Account.class);
        accountService.createAccount(accountDto);
        Mockito.verify(accountRepository, Mockito.times(1)).save(accountEntityArgumentCaptor.capture());
        var account = accountEntityArgumentCaptor.getValue();
        assertEquals(ACCOUNT_NAME, account.getName());
        assertEquals(BigDecimal.valueOf(BALANCE), account.getBalance());
        assertEquals(ACCOUNT_TYPE.getValue().toUpperCase(), account.getType());
    }

    @Test
    void getAccountByIdSuccess() {
        var accountId = UUID.randomUUID();
        var account = getAccount();
        Mockito.when(accountRepository.findByIdAndTenantId(accountId, UserContext.getUserContextData().tenantId())).thenReturn(Optional.of(account));
        var accountDto = accountService.getAccountById(accountId);
        assertEquals(ACCOUNT_NAME, accountDto.getName());
        assertEquals(BALANCE, accountDto.getBalance());
        assertEquals(ACCOUNT_TYPE, accountDto.getType());
    }

    @Test
    void getAccountByIdFails() {
        var accountId = UUID.randomUUID();
        Mockito.when(accountRepository.findByIdAndTenantId(accountId, UserContext.getUserContextData().tenantId())).thenReturn(Optional.empty());
        assertThrows( AccountNotFoundException.class, () -> accountService.getAccountById(accountId));
    }

    @Test
    void getAllAccountsSuccess() {
        Page<Account> page = new PageImpl<>(List.of(getAccount()), PageRequest.of(0, 10), 1);
        Mockito.when(accountRepository.findAllByTenantId(ArgumentMatchers.eq(UserContext.getUserContextData().tenantId()), ArgumentMatchers.any())).thenReturn(page);
        var accountPage = accountService.getAllAccounts(Mockito.mock(Pageable.class));
        Mockito.verify(accountRepository, Mockito.times(1)).findAllByTenantId(ArgumentMatchers.eq(UserContext.getUserContextData().tenantId()), ArgumentMatchers.any());
        assertEquals(1, accountPage.getTotalElements());
        assertEquals(1, accountPage.getTotalPages());
        assertEquals(ACCOUNT_NAME, accountPage.get().findFirst().get().getName());
        assertEquals(BALANCE, accountPage.get().findFirst().get().getBalance());
        assertEquals(ACCOUNT_TYPE, accountPage.get().findFirst().get().getType());
    }

    @Test
    void testDeleteAccountByIdSuccess() {
        var accountId = UUID.randomUUID();
        Mockito.when(accountRepository.deleteByIdAndTenantId(accountId, UserContext.getUserContextData().tenantId())).thenReturn(1);
        accountService.deleteAccountById(accountId);
        Mockito.verify(accountRepository, Mockito.times(1)).deleteByIdAndTenantId(accountId, UserContext.getUserContextData().tenantId());
    }

    @Test
    void testDeleteAccountByIdFails() {
        var accountId = UUID.randomUUID();
        Mockito.when(accountRepository.deleteByIdAndTenantId(accountId, UserContext.getUserContextData().tenantId())).thenReturn(0);
        assertThrows( AccountNotFoundException.class, () -> accountService.deleteAccountById(accountId));
        Mockito.verify(accountRepository, Mockito.times(1)).deleteByIdAndTenantId(accountId, UserContext.getUserContextData().tenantId());
    }

    @Test
    void testDeleteAllAccounts() {
        accountService.deleteAllAccounts();
        Mockito.verify(accountRepository, Mockito.times(1)).deleteAllByTenantId(UserContext.getUserContextData().tenantId());
    }

    @Test
    void testUpdateAccountSuccess() {
        var accountId = UUID.randomUUID();
        var existingAccount = getAccount();
        existingAccount.setId(accountId);
        Mockito.when(accountRepository.findByIdAndTenantId(accountId, UserContext.getUserContextData().tenantId())).thenReturn(Optional.of(existingAccount));
        var updatedAccountDto = new AccountDto();
        updatedAccountDto.setId(accountId);
        updatedAccountDto.setName("updatedName");
        updatedAccountDto.setBalance(100.0);
        updatedAccountDto.setType(AccountType.CASH);
        updatedAccountDto.setIncludeInBudget(false);

        accountService.updateAccount(updatedAccountDto);

        ArgumentCaptor<Account> accountEntityArgumentCaptor = ArgumentCaptor.forClass(Account.class);
        Mockito.verify(accountRepository, Mockito.times(1)).save(accountEntityArgumentCaptor.capture());
        var savedAccount = accountEntityArgumentCaptor.getValue();
        assertEquals("updatedName", savedAccount.getName());
        assertEquals(BigDecimal.valueOf(100.0), savedAccount.getBalance());
        assertEquals(AccountType.CASH.getValue().toUpperCase(), savedAccount.getType());
        assertFalse(savedAccount.isIncludeInBudget());
    }

    @Test
    void testUpdateAccountFails() {
        var accountId = UUID.randomUUID();
        Mockito.when(accountRepository.findByIdAndTenantId(accountId, UserContext.getUserContextData().tenantId())).thenReturn(Optional.empty());
        var updatedAccountDto = new AccountDto();
        updatedAccountDto.setId(accountId);
        assertThrows( AccountNotFoundException.class, () -> accountService.updateAccount(updatedAccountDto));
    }

    private static Account getAccount() {
        var account = new Account();
        account.setName(ACCOUNT_NAME);
        account.setType(ACCOUNT_TYPE.toString().toUpperCase());
        account.setBalance(BigDecimal.valueOf(BALANCE));
        account.setIncludeInBudget(INCLUDE_IN_BUDGET);
        return account;
    }
}