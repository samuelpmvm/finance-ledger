package com.fintech.finance.ledger.controller;

import com.fintech.finance.ledger.common.tenant.UserContext;
import com.fintech.finance.ledger.entity.User;
import com.fintech.finance.ledger.repository.UserRepository;
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
    public User getMe() {
        UUID userId = UserContext.getUserContextData().userId();
        return userRepository.findById(userId).orElseThrow();
    }
}