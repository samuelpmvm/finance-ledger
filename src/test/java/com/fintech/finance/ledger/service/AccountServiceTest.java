package com.fintech.finance.ledger.service;

import com.fintech.finance.ledger.TenantTestExtension;
import com.fintech.finance.ledger.common.exception.AccountDeletionNotAllowedException;
import com.fintech.finance.ledger.common.exception.AccountNotFoundException;
import com.fintech.finance.ledger.common.tenant.UserContext;
import com.fintech.finance.ledger.common.validator.AccountDeletionPolicy;
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
import static org.mockito.Mockito.doThrow;

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

    @Mock
    private AccountDeletionPolicy accountDeletionPolicy;

    private final AccountMapper accountMapper = Mappers.getMapper(AccountMapper.class);

    @BeforeEach
    void setup() {
        accountService = new AccountService(accountRepository, accountMapper, accountDeletionPolicy);
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

        var accountDto = accountPage.getContent().get(0);
        assertEquals(ACCOUNT_NAME, accountDto.getName());
        assertEquals(BALANCE, accountDto.getBalance());
        assertEquals(ACCOUNT_TYPE, accountDto.getType());
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
        Mockito.when(accountRepository.getAllByTenantId(UserContext.getUserContextData().tenantId())).thenReturn(List.of());
        accountService.deleteAllAccounts();
        Mockito.verify(accountRepository, Mockito.times(1)).deleteAllByTenantId(UserContext.getUserContextData().tenantId());
    }

    @Test
    void testDeleteAccountByIdFailsWhenAccountHasTransactions() {
        var accountId = UUID.randomUUID();
        var tenantId = UserContext.getUserContextData().tenantId();
        doThrow(new AccountDeletionNotAllowedException("Account with ID: " + accountId + " cannot be deleted as it has associated transactions."))
                .when(accountDeletionPolicy).validateAccountDeletion(tenantId, accountId);

        var exception = assertThrows(AccountDeletionNotAllowedException.class,
                () -> accountService.deleteAccountById(accountId));

        assertTrue(exception.getMessage().contains("cannot be deleted as it has associated transactions"));
        Mockito.verify(accountDeletionPolicy, Mockito.times(1)).validateAccountDeletion(tenantId, accountId);
        Mockito.verify(accountRepository, Mockito.never()).deleteByIdAndTenantId(Mockito.any(), Mockito.any());
    }

    @Test
    void testDeleteAllAccountsFailsWhenAnyAccountHasTransactions() {
        var tenantId = UserContext.getUserContextData().tenantId();
        var account1 = getAccount();
        var account1Id = UUID.randomUUID();
        account1.setId(account1Id);
        var account2 = getAccount();
        var account2Id = UUID.randomUUID();
        account2.setId(account2Id);

        Mockito.when(accountRepository.getAllByTenantId(tenantId)).thenReturn(List.of(account1, account2));
        Mockito.doNothing().when(accountDeletionPolicy).validateAccountDeletion(tenantId, account1Id);
        doThrow(new AccountDeletionNotAllowedException("Account with ID: " + account2Id + " cannot be deleted as it has associated transactions."))
                .when(accountDeletionPolicy).validateAccountDeletion(tenantId, account2Id);

        var exception = assertThrows(AccountDeletionNotAllowedException.class,
                () -> accountService.deleteAllAccounts());

        assertTrue(exception.getMessage().contains("cannot be deleted as it has associated transactions"));
        Mockito.verify(accountDeletionPolicy, Mockito.times(1)).validateAccountDeletion(tenantId, account1Id);
        Mockito.verify(accountDeletionPolicy, Mockito.times(1)).validateAccountDeletion(tenantId, account2Id);
        Mockito.verify(accountRepository, Mockito.never()).deleteAllByTenantId(tenantId);
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

    @Test
    void testArchiveAccountByIdSuccess() {
        var accountId = UUID.randomUUID();
        var existingAccount = getAccount();
        existingAccount.setId(accountId);
        existingAccount.setArchived(false);

        Mockito.when(accountRepository.findByIdAndTenantId(accountId, UserContext.getUserContextData().tenantId()))
                .thenReturn(Optional.of(existingAccount));
        Mockito.when(accountRepository.save(existingAccount)).thenReturn(existingAccount);

        accountService.archiveUnarchiveAccountById(accountId, true);

        ArgumentCaptor<Account> accountEntityArgumentCaptor = ArgumentCaptor.forClass(Account.class);
        Mockito.verify(accountRepository, Mockito.times(1)).save(accountEntityArgumentCaptor.capture());
        var savedAccount = accountEntityArgumentCaptor.getValue();
        assertTrue(savedAccount.isArchived());
    }

    @Test
    void testUnarchiveAccountByIdSuccess() {
        var accountId = UUID.randomUUID();
        var existingAccount = getAccount();
        existingAccount.setId(accountId);
        existingAccount.setArchived(true);

        Mockito.when(accountRepository.findByIdAndTenantId(accountId, UserContext.getUserContextData().tenantId()))
                .thenReturn(Optional.of(existingAccount));
        Mockito.when(accountRepository.save(existingAccount)).thenReturn(existingAccount);

        accountService.archiveUnarchiveAccountById(accountId, false);

        ArgumentCaptor<Account> accountEntityArgumentCaptor = ArgumentCaptor.forClass(Account.class);
        Mockito.verify(accountRepository, Mockito.times(1)).save(accountEntityArgumentCaptor.capture());
        var savedAccount = accountEntityArgumentCaptor.getValue();
        assertFalse(savedAccount.isArchived());
    }

    @Test
    void testArchiveAccountByIdFailsWhenAccountNotFound() {
        var accountId = UUID.randomUUID();
        Mockito.when(accountRepository.findByIdAndTenantId(accountId, UserContext.getUserContextData().tenantId()))
                .thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.archiveUnarchiveAccountById(accountId, true));
        Mockito.verify(accountRepository, Mockito.never()).save(Mockito.any());
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