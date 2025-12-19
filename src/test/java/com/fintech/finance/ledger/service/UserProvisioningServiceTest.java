package com.fintech.finance.ledger.service;

import com.fintech.finance.ledger.entity.User;
import com.fintech.finance.ledger.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class UserProvisioningServiceTest {

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final String PROVIDER = "provider";
    private static final String PREFERRED_USERNAME = "preferred_username";
    private static final String EMAIL = "email";

    @InjectMocks
    private UserProvisioningService userProvisioningService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Jwt jwt;

    @BeforeEach
    void setup() {
        userProvisioningService = new UserProvisioningService(userRepository);
        Mockito.when(jwt.getSubject()).thenReturn(PROVIDER);
    }

    @Test
    void createUserFromJwt() {
        Mockito.when(jwt.getClaimAsString(EMAIL)).thenReturn(EMAIL);
        Mockito.when(jwt.getClaimAsString(PREFERRED_USERNAME)).thenReturn(PREFERRED_USERNAME);
        Mockito.when(userRepository.findByAuthProviderId(PROVIDER)).thenReturn(Optional.empty());

        userProvisioningService.provisionUser(jwt);
        ArgumentCaptor<User> userEntityArgumentCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository, Mockito.times(1)).save(userEntityArgumentCaptor.capture());

        var userEntity = userEntityArgumentCaptor.getValue();
        assertEquals(PREFERRED_USERNAME, userEntity.getName());
        assertEquals(EMAIL, userEntity.getEmail());
        assertEquals(PROVIDER, userEntity.getAuthProviderId());
        assertNotNull(userEntity.getTenantId());
    }

    @Test
    void provisionUserThatAlreadyExists() {
        var userEntity = new User(PROVIDER, TENANT_ID, PREFERRED_USERNAME, EMAIL);
        Mockito.when(userRepository.findByAuthProviderId(PROVIDER)).thenReturn(Optional.of(userEntity));
        userProvisioningService.provisionUser(jwt);
        ArgumentCaptor<User> userEntityArgumentCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository, Mockito.never()).save(ArgumentMatchers.any(User.class));

        assertEquals(PREFERRED_USERNAME, userEntity.getName());
        assertEquals(EMAIL, userEntity.getEmail());
        assertEquals(PROVIDER, userEntity.getAuthProviderId());
        assertEquals(TENANT_ID, userEntity.getTenantId());
    }

}