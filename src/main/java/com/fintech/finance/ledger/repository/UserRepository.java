package com.fintech.finance.ledger.repository;

import com.fintech.finance.ledger.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByAuthProviderId(String authProviderId);
}
