package com.fintech.finance.ledger.service;

import com.fintech.finance.ledger.entity.Tenant;
import com.fintech.finance.ledger.entity.User;
import com.fintech.finance.ledger.repository.TenantRepository;
import com.fintech.finance.ledger.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserProvisioningService {
    private static final String EMAIL = "email";
    private static final String PREFERRED_USERNAME = "preferred_username";
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserProvisioningService.class);

    public UserProvisioningService(UserRepository userRepository, TenantRepository tenantRepository) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
    }

    public User provisionUser(Jwt jwt) {
        var providerId = jwt.getSubject();
        return userRepository.findByAuthProviderId(providerId)
                .orElseGet(() -> createTenantAndUserFromJwt(jwt));
    }

    private User createTenantAndUserFromJwt(Jwt jwt) {
        LOGGER.info("Provisioning new user with provider ID: {}", jwt.getSubject());
        var providerId = jwt.getSubject();
        var tenantId = createTenant(jwt);
        return createUser(jwt, providerId, tenantId);
    }

    private User createUser(Jwt jwt, String providerId, Tenant tenantId) {
        var name = jwt.getClaimAsString(PREFERRED_USERNAME);
        var email = jwt.getClaimAsString(EMAIL);
        LOGGER.info("Creating user with provider ID: {}", jwt.getSubject());
        return userRepository.save(new User(providerId, tenantId.getId(), name, email));
    }

    private Tenant createTenant(Jwt jwt) {
        LOGGER.info("Creating tenant");
        Tenant tenant = new Tenant();
        tenant.setId(UUID.randomUUID());
        tenant.setName(buildTenantName(jwt));
        return tenantRepository.save(tenant);
    }

    private String buildTenantName(Jwt jwt) {
        return Optional.ofNullable(jwt.getClaimAsString("name"))
                .orElse(jwt.getClaimAsString("preferred_username")) + " Workspace";
    }
}
