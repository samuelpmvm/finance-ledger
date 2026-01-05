package com.fintech.finance.ledger.service;

import com.fintech.finance.ledger.BaseIntegrationTest;
import com.fintech.finance.ledger.common.exception.AccountNotFoundException;
import com.fintech.finance.ledger.common.tenant.UserContext;
import com.fintech.finance.ledger.common.tenant.UserContextData;
import com.fintech.finance.ledger.entity.Account;
import com.fintech.finance.ledger.entity.Tenant;
import com.fintech.finance.ledger.entity.User;
import com.fintech.finance.ledger.repository.AccountRepository;
import com.fintech.finance.ledger.repository.TenantRepository;
import com.fintech.finance.ledger.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    private UUID tenantAId;
    private UUID tenantBId;
    private UUID userAId;
    private UUID userBId;

    @BeforeEach
    void setUp() {

        tenantAId = UUID.randomUUID();
        Tenant tenantA = new Tenant();
        tenantA.setId(tenantAId);
        tenantA.setName("Tenant A");
        tenantRepository.save(tenantA);

        User userA = userRepository.save(new User("user-a-auth", tenantAId, "userA", "usera@test.com"));
        userAId = userA.getId();

        tenantBId = UUID.randomUUID();
        Tenant tenantB = new Tenant();
        tenantB.setId(tenantBId);
        tenantB.setName("Tenant B");
        tenantRepository.save(tenantB);

        User userB = userRepository.save(new User("user-b-auth", tenantBId, "userB", "userb@test.com"));
        userBId = userB.getId();
    }

    @AfterEach
    void cleanup() {
        accountRepository.deleteAllByTenantId(tenantAId);
        accountRepository.deleteAllByTenantId(tenantBId);
        userRepository.deleteById(userAId);
        userRepository.deleteById(userBId);
        tenantRepository.deleteById(tenantAId);
        tenantRepository.deleteById(tenantBId);
        UserContext.clear();
    }

    @Test
    void tenantCannotAccessOtherTenantAccounts() {
        Account tenantAAccount = createAccountEntity(tenantAId, "Tenant A Account");
        accountRepository.save(tenantAAccount);

        withTenant(tenantBId, userBId, () -> {
            var accountPage = accountService.getAllAccounts(PageRequest.of(0, 10));
            assertThat(accountPage.getContent()).isEmpty();
            assertThat(accountPage.getTotalElements()).isZero();
        });
    }

    @Test
    void tenantCannotGetOtherTenantAccountById() {
        Account tenantAAccount = createAccountEntity(tenantAId, "Tenant A Account");
        Account savedAccount = accountRepository.save(tenantAAccount);
        UUID tenantAAccountId = savedAccount.getId();

        withTenant(tenantBId, userBId, () -> {
            assertThrows(AccountNotFoundException.class,
                    () -> accountService.getAccountById(tenantAAccountId));
        });
    }

    @Test
    void tenantCannotDeleteOtherTenantAccountById() {
        Account tenantAAccount = createAccountEntity(tenantAId, "Tenant A Account");
        Account savedAccount = accountRepository.save(tenantAAccount);
        UUID tenantAAccountId = savedAccount.getId();

        withTenant(tenantBId, userBId, () -> {
            assertThrows(AccountNotFoundException.class,
                    () -> accountService.deleteAccountById(tenantAAccountId));
        });


        withTenant(tenantAId, userAId, () -> {
            var account = accountService.getAccountById(tenantAAccountId);
            assertThat(account).isNotNull();
            assertThat(account.getName()).isEqualTo("Tenant A Account");
        });
    }

    @Test
    void tenantCanOnlyAccessOwnAccounts() {

        Account tenantAAccount = createAccountEntity(tenantAId, "Tenant A Account");
        accountRepository.save(tenantAAccount);

        Account tenantBAccount = createAccountEntity(tenantBId, "Tenant B Account");
        accountRepository.save(tenantBAccount);


        withTenant(tenantAId, userAId, () -> {
            var accountPage = accountService.getAllAccounts(PageRequest.of(0, 10));
            assertThat(accountPage.getContent()).hasSize(1);
            assertThat(accountPage.getContent().get(0).getName()).isEqualTo("Tenant A Account");
        });


        withTenant(tenantBId, userBId, () -> {
            var accountPage = accountService.getAllAccounts(PageRequest.of(0, 10));
            assertThat(accountPage.getContent()).hasSize(1);
            assertThat(accountPage.getContent().get(0).getName()).isEqualTo("Tenant B Account");
        });
    }

    private void withTenant(UUID tenantId, UUID userId, Runnable action) {
        UserContext.setUserContextData(new UserContextData(userId, tenantId, "test-auth"));
        try {
            action.run();
        } finally {
            UserContext.clear();
        }
    }

    private Account createAccountEntity(UUID tenantId, String name) {
        Account account = new Account();
        account.setTenantId(tenantId);
        account.setName(name);
        account.setType("BANK");
        account.setBalance(BigDecimal.valueOf(1000.0));
        account.setIncludeInBudget(true);
        account.setArchived(false);
        return account;
    }
}

