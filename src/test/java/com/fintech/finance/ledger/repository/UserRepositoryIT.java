package com.fintech.finance.ledger.repository;

import com.fintech.finance.ledger.BaseIntegrationTest;
import com.fintech.finance.ledger.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryIT extends BaseIntegrationTest {

    @Autowired
    UserRepository repository;

    @Test
    void shouldSaveAndFindByAuthProviderId() {
        User user = new User(
                "kc-123",
                UUID.randomUUID(),
                "test",
                "test@test.com"
        );

        repository.save(user);

        Optional<User> found =
                repository.findByAuthProviderId("kc-123");

        assertTrue(found.isPresent());
    }

}