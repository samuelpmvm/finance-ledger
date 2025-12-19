package com.fintech.finance.ledger.service;

import com.fintech.finance.ledger.entity.User;
import com.fintech.finance.ledger.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional
public class UserProvisioningService {
    private static final String EMAIL = "email";
    private static final String PREFERRED_USERNAME = "preferred_username";
    private final UserRepository userRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserProvisioningService.class);

    public UserProvisioningService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User provisionUser(Jwt jwt) {
        var providerId = jwt.getSubject();
        return userRepository.findByAuthProviderId(providerId)
                .orElseGet(() -> createUserFromJwt(jwt));
    }

    private User createUserFromJwt(Jwt jwt) {
        LOGGER.info("Provisioning new user with provider ID: {}", jwt.getSubject());
        var providerId = jwt.getSubject();
        var tenantId = UUID.randomUUID();
        var name = jwt.getClaimAsString(PREFERRED_USERNAME);
        var email = jwt.getClaimAsString(EMAIL);
        return userRepository.save(new User(providerId, tenantId, name, email));
    }
}
