package com.fintech.finance.ledger.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fintech.finance.ledger.BaseIntegrationTest;
import com.fintech.finance.ledger.common.tenant.UserContext;
import com.fintech.finance.ledger.common.tenant.UserContextData;
import com.fintech.finance.ledger.entity.Tenant;
import com.fintech.finance.ledger.entity.User;
import com.fintech.finance.ledger.repository.TransactionRepository;
import com.fintech.finance.ledger.repository.TenantRepository;
import com.fintech.finance.ledger.repository.UserRepository;
import com.model.transaction.TransactionDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class TransactionControllerTest extends BaseIntegrationTest {

    private static final String API_V1_TRANSACTIONS = "/api/v1/transactions";
    private static final UUID TEST_ACCOUNT_ID = UUID.randomUUID();
    private static final UUID TEST_CATEGORY_ID = UUID.randomUUID();
    private static final double AMOUNT_1 = 150.50;
    private static final double AMOUNT_2 = 75.25;
    private static final String DESCRIPTION_1 = "Grocery shopping";
    private static final String DESCRIPTION_2 = "Gas station";
    private static final String AUTH_PROVIDER_ID = "kc-user-id";
    private static final String TEST_EMAIL = "test@test.com";
    private static final MediaType TRANSACTION_REQUEST_JSON = MediaType.parseMediaType("application/transaction-request+json");

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TenantRepository tenantRepository;

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
        userRepository.deleteById(userId);
        tenantRepository.deleteById(tenantId);
        UserContext.clear();
    }

    @Test
    void shouldCreateTransaction() throws Exception {
        TransactionDto transactionDto = createTransactionDto(AMOUNT_1, DESCRIPTION_1);

        mockMvc.perform(post(API_V1_TRANSACTIONS)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        }))
                        .contentType(TRANSACTION_REQUEST_JSON)
                        .content(objectMapper.writeValueAsString(transactionDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(AMOUNT_1))
                .andExpect(jsonPath("$.description").value(DESCRIPTION_1))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void shouldReturnPagedTransactions() throws Exception {
        createTestTransaction(AMOUNT_1, DESCRIPTION_1);
        createTestTransaction(AMOUNT_2, DESCRIPTION_2);

        mockMvc.perform(get(API_V1_TRANSACTIONS)
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
    void shouldGetTransactionById() throws Exception {
        UUID transactionId = createTestTransaction(AMOUNT_1, DESCRIPTION_1);

        mockMvc.perform(get(API_V1_TRANSACTIONS + "/{transactionId}", transactionId)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        })))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId.toString()))
                .andExpect(jsonPath("$.amount").value(AMOUNT_1))
                .andExpect(jsonPath("$.description").value(DESCRIPTION_1));
    }

    @Test
    void shouldReturnNotFoundForNonExistentTransaction() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get(API_V1_TRANSACTIONS + "/{transactionId}", nonExistentId)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        })))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteTransactionById() throws Exception {
        UUID transactionId = createTestTransaction(AMOUNT_1, DESCRIPTION_1);

        mockMvc.perform(delete(API_V1_TRANSACTIONS + "/{transactionId}", transactionId)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        })))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(API_V1_TRANSACTIONS + "/{transactionId}", transactionId)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        })))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteAllTransactions() throws Exception {
        createTestTransaction(AMOUNT_1, DESCRIPTION_1);
        createTestTransaction(AMOUNT_2, DESCRIPTION_2);

        mockMvc.perform(delete(API_V1_TRANSACTIONS)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        })))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(API_V1_TRANSACTIONS)
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
    void shouldUpdateTransaction() throws Exception {
        UUID transactionId = createTestTransaction(AMOUNT_1, DESCRIPTION_1);

        TransactionDto updateDto = createTransactionDto(200.0, "Updated description");
        updateDto.setId(transactionId);

        mockMvc.perform(put(API_V1_TRANSACTIONS)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        }))
                        .contentType(TRANSACTION_REQUEST_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(200.0))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    void shouldReturnUnauthorizedWithoutJwt() throws Exception {
        mockMvc.perform(get(API_V1_TRANSACTIONS)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isUnauthorized());
    }

    private TransactionDto createTransactionDto(double amount, String description) {
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setAccountId(TEST_ACCOUNT_ID);
        transactionDto.setAmount(amount);
        transactionDto.setDate(LocalDate.of(2026, 1, 5));
        transactionDto.setDescription(description);
        transactionDto.setCategoryId(TEST_CATEGORY_ID);
        transactionDto.setImported(false);
        return transactionDto;
    }

    private UUID createTestTransaction(double amount, String description) throws Exception {
        TransactionDto transactionDto = createTransactionDto(amount, description);

        String response = mockMvc.perform(post(API_V1_TRANSACTIONS)
                        .with(jwt().jwt(jwt -> {
                            jwt.subject(AUTH_PROVIDER_ID);
                            jwt.claim("email", TEST_EMAIL);
                        }))
                        .contentType(TRANSACTION_REQUEST_JSON)
                        .content(objectMapper.writeValueAsString(transactionDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, TransactionDto.class).getId();
    }
}

