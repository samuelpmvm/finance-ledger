package com.fintech.finance.ledger.service;

import com.fintech.finance.ledger.BaseIntegrationTest;
import com.fintech.finance.ledger.entity.User;
import com.fintech.finance.ledger.repository.TenantRepository;
import com.fintech.finance.ledger.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserProvisioningServiceIT extends BaseIntegrationTest {
    @Autowired
    UserProvisioningService service;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TenantRepository tenantRepository;

    @Test
    void shouldProvisionUserOnce() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "kc-999")
                .claim("email", "int@test.com")
                .claim("preferred_username", "integration")
                .build();

        User first = service.provisionUser(jwt);
        User second = service.provisionUser(jwt);

        assertEquals(first.getId(), second.getId());
        assertEquals(1, userRepository.count());
        assertEquals(1, tenantRepository.count());
    }
}
