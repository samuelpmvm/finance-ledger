package com.fintech.finance.ledger.service;

import com.fintech.finance.ledger.entity.Tenant;
import com.fintech.finance.ledger.entity.User;
import com.fintech.finance.ledger.repository.TenantRepository;
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
    public static final String NAME = "name";

    @InjectMocks
    private UserProvisioningService userProvisioningService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private Jwt jwt;

    @BeforeEach
    void setup() {
        userProvisioningService = new UserProvisioningService(userRepository, tenantRepository);
        Mockito.when(jwt.getSubject()).thenReturn(PROVIDER);
    }

    @Test
    void createTenantAndUserFromJwt() {
        Mockito.when(jwt.getClaimAsString(EMAIL)).thenReturn(EMAIL);
        Mockito.when(jwt.getClaimAsString(PREFERRED_USERNAME)).thenReturn(PREFERRED_USERNAME);
        Mockito.when(jwt.getClaimAsString(NAME)).thenReturn(null);
        Mockito.when(userRepository.findByAuthProviderId(PROVIDER)).thenReturn(Optional.empty());
        var tenant = new Tenant();
        var tenantId = UUID.randomUUID();
        tenant.setId(tenantId);
        Mockito.when(tenantRepository.save(ArgumentMatchers.any(Tenant.class))).thenReturn(tenant);

        userProvisioningService.provisionUser(jwt);
        ArgumentCaptor<User> userEntityArgumentCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository, Mockito.times(1)).save(userEntityArgumentCaptor.capture());
        Mockito.verify(tenantRepository, Mockito.times(1)).save(ArgumentMatchers.any(Tenant.class));

        var userEntity = userEntityArgumentCaptor.getValue();
        assertEquals(PREFERRED_USERNAME, userEntity.getName());
        assertEquals(EMAIL, userEntity.getEmail());
        assertEquals(PROVIDER, userEntity.getAuthProviderId());
        assertEquals(tenantId, userEntity.getTenantId());
        assertNotNull(userEntity.getTenantId());
    }

    @Test
    void provisionUserThatAlreadyExists() {
        var userEntity = new User(PROVIDER, TENANT_ID, PREFERRED_USERNAME, EMAIL);
        Mockito.when(userRepository.findByAuthProviderId(PROVIDER)).thenReturn(Optional.of(userEntity));
        userProvisioningService.provisionUser(jwt);
        ArgumentCaptor<User> userEntityArgumentCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository, Mockito.never()).save(ArgumentMatchers.any(User.class));
        Mockito.verify(tenantRepository, Mockito.never()).save(ArgumentMatchers.any(Tenant.class));

        assertEquals(PREFERRED_USERNAME, userEntity.getName());
        assertEquals(EMAIL, userEntity.getEmail());
        assertEquals(PROVIDER, userEntity.getAuthProviderId());
        assertEquals(TENANT_ID, userEntity.getTenantId());
    }

}