package com.fintech.finance.ledger.userauth.dto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "provider_id", nullable = false, unique = true)
    private String providerId;

    @Column(nullable = false)
    private String email;

    private String name;

    public UserEntity(String providerId, String email, String name) {
        this.providerId = providerId;
        this.email = email;
        this.name = name;
    }
}
