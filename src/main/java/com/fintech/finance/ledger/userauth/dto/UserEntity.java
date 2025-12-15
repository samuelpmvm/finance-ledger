package com.fintech.finance.ledger.userauth.dto;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "provider_id", nullable = false, unique = true)
    private String providerId;

    @Column(nullable = false)
    private String email;

    private String name;

    protected UserEntity() {}

    public UserEntity(String providerId, String email, String name) {
        this.providerId = providerId;
        this.email = email;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getName() {
        return name;
    }
}
