package com.fintech.finance.ledger.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenants")
@Getter @Setter @NoArgsConstructor
public class Tenant {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    private Instant createdAt = Instant.now();
}

