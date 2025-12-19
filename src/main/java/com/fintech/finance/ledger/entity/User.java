package com.fintech.finance.ledger.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "authProvider_id", nullable = false, unique = true)
    private String authProviderId;

    private String name;

    @Column(nullable = false)
    private String email;

    @OneToMany(mappedBy = "user")
    private Set<Account> accounts = new HashSet<>();

    public User(String authProviderId, UUID tenantId, String name, String email) {
        this.authProviderId = authProviderId;
        this.tenantId = tenantId;
        this.email = email;
        this.name = name;
    }
}
