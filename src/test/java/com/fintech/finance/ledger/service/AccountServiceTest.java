package com.fintech.finance.ledger.service;

import com.fintech.finance.ledger.common.exception.AccountNotFoundException;
import com.fintech.finance.ledger.common.tenant.UserContext;
import com.fintech.finance.ledger.common.tenant.UserContextData;
import com.fintech.finance.ledger.entity.Account;
import com.fintech.finance.ledger.mapper.AccountMapper;
import com.fintech.finance.ledger.repository.AccountRepository;
import com.model.accounts.AccountDto;
import com.model.accounts.AccountType;
import org.apache.tomcat.util.http.parser.TE;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class AccountServiceTest {

    private static final String ACCOUNT_NAME = "account";
    private static final BigDecimal BALANCE = BigDecimal.valueOf(0.0);
    private static final AccountType ACCOUNT_TYPE = AccountType.BANK;
    private static final boolean INCLUDE_IN_BUDGET = true;
    private static final UUID TEST_TENANT_ID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID TEST_USER_ID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String USER_AUTH = "user";

    @InjectMocks
    private AccountService accountService;

    @Mock
    private AccountRepository accountRepository;

    private final AccountMapper accountMapper = Mappers.getMapper(AccountMapper.class);

    @BeforeEach
    void setup() {
        var userContextData = new UserContextData(TEST_USER_ID, TEST_TENANT_ID, USER_AUTH);
        UserContext.setUserContextData(userContextData);
        accountService = new AccountService(accountRepository, accountMapper);
    }

    @AfterEach
    void cleanup() {
        UserContext.clear();
    }

    @Test
    void createAccountSuccess() {
        var accountDto = new AccountDto(ACCOUNT_NAME, BALANCE.toString(), ACCOUNT_TYPE, INCLUDE_IN_BUDGET);
        ArgumentCaptor<Account> accountEntityArgumentCaptor = ArgumentCaptor.forClass(Account.class);
        accountService.createAccount(accountDto);
        Mockito.verify(accountRepository, Mockito.times(1)).save(accountEntityArgumentCaptor.capture());
        var account = accountEntityArgumentCaptor.getValue();
        assertEquals(ACCOUNT_NAME, account.getName());
        assertEquals(BALANCE, account.getBalance());
        assertEquals(ACCOUNT_TYPE.getValue().toUpperCase(), account.getType());
    }

    @Test
    void getAccountByIdSuccess() {
        var accountId = UUID.randomUUID();
        var account = new Account();
        account.setName(ACCOUNT_NAME);
        account.setType(ACCOUNT_TYPE.toString().toUpperCase());
        account.setBalance(BALANCE);
        account.setIncludeInBudget(INCLUDE_IN_BUDGET);
        Mockito.when(accountRepository.findByIdAndTenantId(accountId, TEST_TENANT_ID)).thenReturn(Optional.of(account));
        var accountDto = accountService.findAccountById(accountId);
        assertEquals(ACCOUNT_NAME, accountDto.getName());
        assertEquals(BALANCE.toString(), accountDto.getBalance());
        assertEquals(ACCOUNT_TYPE, accountDto.getType());
    }

    @Test
    void getAccountByIdFails() {
        var accountId = UUID.randomUUID();
        Mockito.when(accountRepository.findByIdAndTenantId(accountId, TEST_TENANT_ID)).thenReturn(Optional.empty());
        assertThrows( AccountNotFoundException.class, () -> accountService.findAccountById(accountId));
    }
}