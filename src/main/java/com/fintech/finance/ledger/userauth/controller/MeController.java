package com.fintech.finance.ledger.userauth.controller;

import com.fintech.finance.ledger.common.tenant.TenantContext;
import com.fintech.finance.ledger.userauth.dto.UserEntity;
import com.fintech.finance.ledger.userauth.repositories.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class MeController {

    private final UserRepository userRepository;

    public MeController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public UserEntity getMe() {
        UUID userId = TenantContext.getUserId();
        return userRepository.findById(userId).orElseThrow();
    }
}