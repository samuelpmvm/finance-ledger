package com.fintech.finance.ledger.userauth.controller;

import com.fintech.finance.ledger.common.tenant.TenantContext;
import com.fintech.finance.ledger.userauth.dto.UserEntity;
import com.fintech.finance.ledger.userauth.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MeController.class)
@Tag("unit")
@ActiveProfiles("test")
class MeControllerTest {

    private static final String PROVIDER_ID = "provider-123";
    private static final String EMAIL = "test@example.com";
    private static final String TESTUSER = "testuser";
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @WithMockUser
    void testGetMe_ReturnsUserSuccessfully() throws Exception {
        UUID userId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity(PROVIDER_ID, EMAIL, TESTUSER);
        userEntity.setId(userId);

        TenantContext.setUserId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        mockMvc.perform(get("/api/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.name").value(TESTUSER))
                .andExpect(jsonPath("$.providerId").value(PROVIDER_ID));
    }

    @Test
    void testGetMe_WithoutAuthentication_ReturnsUnauthorized() throws Exception {

        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized());
    }
}

