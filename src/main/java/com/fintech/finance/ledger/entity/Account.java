package com.fintech.finance.ledger.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "accounts",
        indexes = @Index(name = "idx_account_tenant", columnList = "tenant_id"))
@Getter @Setter @NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String name;

    private String type; // e.g., BANK, CASH, INVESTMENT, SAVINGS

    @Column(nullable = false)
    private boolean includeInBudget;

    @Column(nullable = false)
    private BigDecimal balance;
}

