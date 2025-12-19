package com.fintech.finance.ledger.controller;

import com.fintech.finance.ledger.common.tenant.UserContext;
import com.fintech.finance.ledger.common.tenant.UserContextData;
import com.fintech.finance.ledger.controller.MeController;
import com.fintech.finance.ledger.entity.User;
import com.fintech.finance.ledger.repository.UserRepository;
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

    private static final String AUTHPROVIDER_ID = "provider-id";
    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final String EMAIL = "test@example.com";
    private static final String TESTUSER = "testuser";
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @WithMockUser
    void testGetMe_ReturnsUserSuccessfully() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = new User(AUTHPROVIDER_ID, TENANT_ID, TESTUSER, EMAIL);
        user.setId(userId);

        UserContext.setUserContextData(new UserContextData( user.getId(), user.getTenantId(), user.getAuthProviderId()));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.name").value(TESTUSER))
                .andExpect(jsonPath("$.authProviderId").value(AUTHPROVIDER_ID));
    }

    @Test
    void testGetMe_WithoutAuthentication_ReturnsUnauthorized() throws Exception {

        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized());
    }
}

