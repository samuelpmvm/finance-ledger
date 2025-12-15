package com.fintech.finance.ledger.userauth.service;

import com.fintech.finance.ledger.userauth.dto.UserEntity;
import com.fintech.finance.ledger.userauth.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

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

    public UserEntity provisionUser(Jwt jwt) {
        var providerId = jwt.getSubject();
        return userRepository.findByProviderId(providerId)
                .orElseGet(() -> createUserFromJwt(jwt));
    }

    private UserEntity createUserFromJwt(Jwt jwt) {
        LOGGER.info("Provisioning new user with provider ID: {}", jwt.getSubject());
        var providerId = jwt.getSubject();
        var email = jwt.getClaimAsString(EMAIL);
        var name = jwt.getClaimAsString(PREFERRED_USERNAME);
        return userRepository.save(new UserEntity(providerId, email, name));
    }
}
