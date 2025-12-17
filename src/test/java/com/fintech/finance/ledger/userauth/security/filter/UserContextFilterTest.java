package com.fintech.finance.ledger.userauth.security.filter;

import com.fintech.finance.ledger.common.tenant.TenantContext;
import com.fintech.finance.ledger.userauth.dto.UserEntity;
import com.fintech.finance.ledger.userauth.service.UserProvisioningService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@ActiveProfiles("test")
class UserContextFilterTest {

    private static final String PROVIDER_ID = "provider-id";
    private static final String EMAIL = "test@example.com";
    private static final String TESTUSER = "testuser";

    @InjectMocks
    private UserContextFilter userContextFilter;

    @Mock
    private UserProvisioningService provisioningService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testDoFilterInternalWithValidJwtAuthentication() throws Exception {
        UUID userId = UUID.randomUUID();
        Jwt jwt = createMockJwt();
        UserEntity userEntity = new UserEntity(PROVIDER_ID, EMAIL, TESTUSER);
        userEntity.setId(userId);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(provisioningService.provisionUser(jwt)).thenReturn(userEntity);

        userContextFilter.doFilterInternal(request, response, filterChain);

        verify(provisioningService, times(1)).provisionUser(jwt);
        verify(filterChain, times(1)).doFilter(request, response);

        assertNull(TenantContext.getUserId());
    }

    @Test
    void testDoFilterInternalWithNoAuthentication() throws Exception {
        when(securityContext.getAuthentication()).thenReturn(null);

        userContextFilter.doFilterInternal(request, response, filterChain);

        verify(provisioningService, never()).provisionUser(any());
        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(TenantContext.getUserId());
    }

    @Test
    void testDoFilterInternalWithNonJwtPrincipal() throws Exception {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("not-a-jwt-principal");

        userContextFilter.doFilterInternal(request, response, filterChain);

        verify(provisioningService, never()).provisionUser(any());
        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(TenantContext.getUserId());
    }

    @Test
    void testDoFilterInternalClearsTenantContextEvenOnException() throws ServletException, IOException {
        UUID userId = UUID.randomUUID();
        Jwt jwt = createMockJwt();
        UserEntity userEntity = new UserEntity(PROVIDER_ID, EMAIL, TESTUSER);
        userEntity.setId(userId);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(provisioningService.provisionUser(jwt)).thenReturn(userEntity);
        doThrow(new ServletException("Test exception")).when(filterChain).doFilter(request, response);

        assertThrows(ServletException.class, () -> userContextFilter.doFilterInternal(request, response, filterChain));

        assertNull(TenantContext.getUserId());
    }

    @Test
    void testDoFilterInternalSetsUserIdInTenantContext() throws ServletException, IOException {
        UUID userId = UUID.randomUUID();
        Jwt jwt = createMockJwt();
        UserEntity userEntity = new UserEntity(PROVIDER_ID, EMAIL, TESTUSER);
        userEntity.setId(userId);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(provisioningService.provisionUser(jwt)).thenReturn(userEntity);

        doAnswer(invocation -> {
            assertEquals(userId, TenantContext.getUserId());return null;})
                .when(filterChain)
                .doFilter(request, response);

        userContextFilter.doFilterInternal(request, response, filterChain);

        verify(provisioningService, times(1)).provisionUser(jwt);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    private static Jwt createMockJwt() {
        return new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "RS256"),
                Map.of("sub", "user-123", "email", EMAIL, "preferred_username", TESTUSER)
        );
    }
}