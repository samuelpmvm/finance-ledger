package com.fintech.finance.ledger.userauth.repositories;

import com.fintech.finance.ledger.BaseIntegrationTest;
import com.fintech.finance.ledger.userauth.dto.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryIT extends BaseIntegrationTest {

    @Autowired
    UserRepository repository;

    @Test
    void shouldSaveAndFindByProviderId() {
        UserEntity user = new UserEntity(
                "kc-123",
                "test@test.com",
                "test"
        );

        repository.save(user);

        Optional<UserEntity> found =
                repository.findByProviderId("kc-123");

        assertTrue(found.isPresent());
    }

}