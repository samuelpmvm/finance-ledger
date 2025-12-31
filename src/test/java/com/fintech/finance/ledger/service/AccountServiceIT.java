package com.fintech.finance.ledger.service;

import com.fintech.finance.ledger.BaseIntegrationTest;
import com.fintech.finance.ledger.TenantTestExtension;
import com.fintech.finance.ledger.common.tenant.UserContext;
import com.fintech.finance.ledger.repository.AccountRepository;
import com.fintech.finance.ledger.repository.TenantRepository;
import com.model.accounts.AccountDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(TenantTestExtension.class)
class AccountServiceIT extends BaseIntegrationTest {

    private static final String TEST_ACCOUNT_1 = "Test Account 1";
    private static final double BALANCE_1 = 1000.0;
    private static final String TEST_ACCOUNT_2 = "Test Account 2";
    private static final double BALANCE_2 = 50.0;
    @Autowired
    private AccountService service;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @AfterEach
    void cleanup() {
        var tenantId = UserContext.getUserContextData().tenantId();
        accountRepository.deleteAllByTenantId(tenantId);
    }

    @Test
    void testCreateAccount() {
        AccountDto accountDto = new AccountDto();
        accountDto.setName(TEST_ACCOUNT_1);
        accountDto.setBalance(BALANCE_1);

        AccountDto createdAccount = service.createAccount(accountDto);

        assertNotNull(createdAccount);
        assertNotNull(createdAccount.getId());
        assertEquals(TEST_ACCOUNT_1, createdAccount.getName());
        assertEquals(BALANCE_1, createdAccount.getBalance());
    }

    @Test
    void testGetAccountById() {
        AccountDto accountDto = new AccountDto();
        accountDto.setName(TEST_ACCOUNT_1);
        accountDto.setBalance(BALANCE_1);

        AccountDto createdAccount = service.createAccount(accountDto);
        var foundAccountDto = service.getAccountById(createdAccount.getId());

        assertEquals(createdAccount.getId(), foundAccountDto.getId());
        assertEquals(TEST_ACCOUNT_1, foundAccountDto.getName());
        assertEquals(BALANCE_1, foundAccountDto.getBalance());
    }

    @Test
    void testGetAllAccounts() {
        var accountDtoFirst = new AccountDto();
        accountDtoFirst.setName(TEST_ACCOUNT_1);
        accountDtoFirst.setBalance(BALANCE_1);
        service.createAccount(accountDtoFirst);

        var accountDtoSecond = new AccountDto();
        accountDtoSecond.setName(TEST_ACCOUNT_2);
        accountDtoSecond.setBalance(BALANCE_2);
        service.createAccount(accountDtoSecond);

        var pageRequest = PageRequest.of(0, 10);

        var accountPage = service.getAllAccounts(pageRequest);

        assertEquals(2, accountPage.getTotalElements());
        var createdAccount = accountPage.getContent().get(0);
        assertEquals(TEST_ACCOUNT_1, createdAccount.getName());
        assertEquals(BALANCE_1, createdAccount.getBalance());
        assertEquals(TEST_ACCOUNT_2, accountPage.getContent().get(1).getName());
        assertEquals(BALANCE_2,  accountPage.getContent().get(1).getBalance());
    }

    @Test
    void testDeleteAccountById() {
        AccountDto accountDto = new AccountDto();
        accountDto.setName(TEST_ACCOUNT_1);
        accountDto.setBalance(BALANCE_1);

        AccountDto createdAccount = service.createAccount(accountDto);
        service.deleteAccountById(createdAccount.getId());

        var tenantId = UserContext.getUserContextData().tenantId();
        var accounts =  accountRepository.findAllByTenantId(tenantId, PageRequest.of(0, 10));
        assertEquals(0, accounts.getTotalElements());
    }

    @Test
    void testDeleteAllAccounts() {
        var accountDtoFirst = new AccountDto();
        accountDtoFirst.setName(TEST_ACCOUNT_1);
        accountDtoFirst.setBalance(BALANCE_1);
        service.createAccount(accountDtoFirst);

        var accountDtoSecond = new AccountDto();
        accountDtoSecond.setName(TEST_ACCOUNT_2);
        accountDtoSecond.setBalance(BALANCE_2);
        service.createAccount(accountDtoSecond);

        service.deleteAllAccounts();

        var tenantId = UserContext.getUserContextData().tenantId();
        var accounts =  accountRepository.findAllByTenantId(tenantId, PageRequest.of(0, 10));
        assertEquals(0, accounts.getTotalElements());
    }

    @Test
    void testUpdateAccount() {
        AccountDto accountDto = new AccountDto();
        accountDto.setName(TEST_ACCOUNT_1);
        accountDto.setBalance(BALANCE_1);

        AccountDto createdAccount = service.createAccount(accountDto);

        AccountDto updateDto = new AccountDto();
        updateDto.setId(createdAccount.getId());
        updateDto.setName("Updated Account Name");
        updateDto.setBalance(2000.0);

        service.updateAccount(updateDto);

        var updatedAccount = service.getAccountById(createdAccount.getId());
        assertEquals("Updated Account Name", updatedAccount.getName());
        assertEquals(2000.0, updatedAccount.getBalance());
    }
}
