package com.fintech.finance.ledger.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintech.finance.ledger.BaseIntegrationTest;
import com.fintech.finance.ledger.common.tenant.UserContext;
import com.fintech.finance.ledger.common.tenant.UserContextData;
import com.fintech.finance.ledger.entity.Tenant;
import com.fintech.finance.ledger.entity.Transaction;
import com.fintech.finance.ledger.entity.User;
import com.fintech.finance.ledger.repository.AccountRepository;
import com.fintech.finance.ledger.repository.TenantRepository;
import com.fintech.finance.ledger.repository.TransactionRepository;
import com.fintech.finance.ledger.repository.UserRepository;
import com.model.accounts.AccountDto;
import com.model.accounts.AccountType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AccountControllerTest extends BaseIntegrationTest {

    private static final String API_V1_ACCOUNTS = "/api/v1/accounts";
    private static final String TEST_ACCOUNT_1 = "Test Account 1";
    private static final String TEST_ACCOUNT_2 = "Test Account 2";
    private static final double BALANCE_1 = 1000.0;
    private static final double BALANCE_2 = 50.0;
    private static final String AUTH_PROVIDER_ID = "kc-user-id";
    private static final String TEST_EMAIL = "test@test.com";
    private static final MediaType ACCOUNT_REQUEST_JSON = MediaType.parseMediaType("application/account-request+json");

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    private UUID tenantId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        Tenant tenant = new Tenant();
        tenant.setId(tenantId);
        tenant.setName("Test Tenant");
        tenantRepository.save(tenant);

        User user = userRepository.save(new User(AUTH_PROVIDER_ID, tenantId, "testuser", TEST_EMAIL));
        userId = user.getId();

        UserContext.setUserContextData(new UserContextData(userId, tenantId, AUTH_PROVIDER_ID));
    }

    @AfterEach
    void cleanup() {
        transactionRepository.deleteAllByTenantId(tenantId);
        accountRepository.deleteAllByTenantId(tenantId);
        userRepository.deleteById(userId);
        tenantRepository.deleteById(tenantId);
        UserContext.clear();
    }

    @Test
    void shouldCreateAccount() throws Exception {
        AccountDto accountDto = createAccountDto(TEST_ACCOUNT_1, BALANCE_1);

        mockMvc.perform(post(API_V1_ACCOUNTS)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        }))
                        .contentType(ACCOUNT_REQUEST_JSON)
                        .content(objectMapper.writeValueAsString(accountDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(TEST_ACCOUNT_1))
                .andExpect(jsonPath("$.balance").value(BALANCE_1))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void shouldReturnPagedAccounts() throws Exception {
        createTestAccount(TEST_ACCOUNT_1, BALANCE_1);
        createTestAccount(TEST_ACCOUNT_2, BALANCE_2);

        mockMvc.perform(get(API_V1_ACCOUNTS)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        }))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void shouldGetAccountById() throws Exception {
        UUID accountId = createTestAccount(TEST_ACCOUNT_1, BALANCE_1);

        mockMvc.perform(get(API_V1_ACCOUNTS + "/{accountId}", accountId)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        })))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountId.toString()))
                .andExpect(jsonPath("$.name").value(TEST_ACCOUNT_1))
                .andExpect(jsonPath("$.balance").value(BALANCE_1));
    }

    @Test
    void shouldReturnNotFoundForNonExistentAccount() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get(API_V1_ACCOUNTS + "/{accountId}", nonExistentId)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        })))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteAccountById() throws Exception {
        UUID accountId = createTestAccount(TEST_ACCOUNT_1, BALANCE_1);

        mockMvc.perform(delete(API_V1_ACCOUNTS + "/{accountId}", accountId)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        })))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(API_V1_ACCOUNTS + "/{accountId}", accountId)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        })))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteAllAccounts() throws Exception {
        createTestAccount(TEST_ACCOUNT_1, BALANCE_1);
        createTestAccount(TEST_ACCOUNT_2, BALANCE_2);

        mockMvc.perform(delete(API_V1_ACCOUNTS)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        })))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(API_V1_ACCOUNTS)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        }))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void shouldUpdateAccount() throws Exception {
        UUID accountId = createTestAccount(TEST_ACCOUNT_1, BALANCE_1);

        AccountDto updateDto = createAccountDto("Updated Account Name", 2000.0);
        updateDto.setId(accountId);

        mockMvc.perform(put(API_V1_ACCOUNTS)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        }))
                        .contentType(ACCOUNT_REQUEST_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Account Name"))
                .andExpect(jsonPath("$.balance").value(2000.0));
    }

    @Test
    void shouldReturnUnauthorizedWithoutJwt() throws Exception {
        mockMvc.perform(get(API_V1_ACCOUNTS)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotDeleteAccountWithTransactions() throws Exception {
        UUID accountId = createTestAccount(TEST_ACCOUNT_1, BALANCE_1);
        createTestTransaction(accountId);

        mockMvc.perform(delete(API_V1_ACCOUNTS + "/{accountId}", accountId)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        })))
                .andExpect(status().isImUsed())
                .andExpect(jsonPath("$.message").value("Account with ID: " + accountId + " cannot be deleted as it has associated transactions."));
    }

    @Test
    void shouldNotDeleteAllAccountsWhenAnyHasTransactions() throws Exception {
        UUID accountId1 = createTestAccount(TEST_ACCOUNT_1, BALANCE_1);
        createTestAccount(TEST_ACCOUNT_2, BALANCE_2);
        createTestTransaction(accountId1);

        mockMvc.perform(delete(API_V1_ACCOUNTS)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        })))
                .andExpect(status().isImUsed())
                .andExpect(jsonPath("$.message").value("Account with ID: " + accountId1 + " cannot be deleted as it has associated transactions."));

        mockMvc.perform(get(API_V1_ACCOUNTS)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        }))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void shouldArchiveAccountById() throws Exception {
        UUID accountId = createTestAccount(TEST_ACCOUNT_1, BALANCE_1);

        mockMvc.perform(patch(API_V1_ACCOUNTS + "/{accountId}", accountId)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        }))
                        .param("archive", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountId.toString()))
                .andExpect(jsonPath("$.name").value(TEST_ACCOUNT_1))
                .andExpect(jsonPath("$.archived").value(true));
    }

    @Test
    void shouldUnarchiveAccountById() throws Exception {
        UUID accountId = createTestAccount(TEST_ACCOUNT_1, BALANCE_1);

        mockMvc.perform(patch(API_V1_ACCOUNTS + "/{accountId}", accountId)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        }))
                        .param("archive", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.archived").value(true));

        mockMvc.perform(patch(API_V1_ACCOUNTS + "/{accountId}", accountId)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        }))
                        .param("archive", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountId.toString()))
                .andExpect(jsonPath("$.name").value(TEST_ACCOUNT_1))
                .andExpect(jsonPath("$.archived").value(false));
    }

    @Test
    void shouldReturnNotFoundWhenArchivingNonExistentAccount() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(patch(API_V1_ACCOUNTS + "/{accountId}", nonExistentId)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        }))
                        .param("archive", "true"))
                .andExpect(status().isNotFound());
    }

    private static AccountDto createAccountDto(String name, double balance) {
        AccountDto accountDto = new AccountDto();
        accountDto.setName(name);
        accountDto.setBalance(balance);
        accountDto.setType(AccountType.BANK);
        accountDto.setIncludeInBudget(true);
        return accountDto;
    }

    private UUID createTestAccount(String name, double balance) throws Exception {
        AccountDto accountDto = createAccountDto(name, balance);

        String response = mockMvc.perform(post(API_V1_ACCOUNTS)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        }))
                        .contentType(ACCOUNT_REQUEST_JSON)
                        .content(objectMapper.writeValueAsString(accountDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, AccountDto.class).getId();
    }

    private void createTestTransaction(UUID accountId) {
        Transaction transaction = new Transaction();
        transaction.setTenantId(tenantId);
        transaction.setAccountId(accountId);
        transaction.setAmount(BigDecimal.valueOf(100.0));
        transaction.setDate(LocalDate.now());
        transaction.setDescription("Test Transaction");
        transactionRepository.save(transaction);
    }
}


