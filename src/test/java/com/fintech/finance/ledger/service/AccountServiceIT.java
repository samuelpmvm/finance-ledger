package com.fintech.finance.ledger.service;

import com.fintech.finance.ledger.BaseIntegrationTest;
import com.fintech.finance.ledger.common.tenant.UserContext;
import com.fintech.finance.ledger.common.tenant.UserContextData;
import com.fintech.finance.ledger.repository.AccountRepository;
import com.fintech.finance.ledger.repository.TenantRepository;
import com.model.accounts.AccountDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AccountServiceIT extends BaseIntegrationTest {

    @Autowired
    private AccountService service;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private static final UUID TEST_TENANT_ID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID TEST_USER_ID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String USER_AUTH = "user";

    @BeforeEach
    void setup() {
        var userContextData = new UserContextData(TEST_USER_ID, TEST_TENANT_ID, USER_AUTH);
        UserContext.setUserContextData(userContextData);
    }

    @AfterEach
    void cleanup() {
        UserContext.clear();
    }

    @Test
    void testCreateAccount() {
        AccountDto accountDto = new AccountDto();
        accountDto.setName("Test Account");
        accountDto.setBalance(1000.0);

        AccountDto createdAccount = service.createAccount(accountDto);

        assertNotNull(createdAccount);
        assertNotNull(createdAccount.getId());
        assertEquals("Test Account", createdAccount.getName());
        assertEquals(1000.0, createdAccount.getBalance());
    }

    @Test
    void testFindAccountById() {
        AccountDto accountDto = new AccountDto();
        accountDto.setName("Test Account");
        accountDto.setBalance(1000.0);

        AccountDto createdAccount = service.createAccount(accountDto);
        var foundAccountDto = service.findAccountById(createdAccount.getId());

        assertEquals(createdAccount.getId(), foundAccountDto.getId());
        assertEquals("Test Account", foundAccountDto.getName());
        assertEquals(1000.0, foundAccountDto.getBalance());
    }
}
