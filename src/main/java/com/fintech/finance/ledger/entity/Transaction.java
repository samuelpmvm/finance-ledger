package com.fintech.finance.ledger.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "transactions",
        indexes = {
                @Index(name = "idx_tx_tenant", columnList = "tenant_id"),
                @Index(name = "idx_tx_account", columnList = "account_id"),
                @Index(name = "idx_tx_category", columnList = "category_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "tx_date", nullable = false)
    private LocalDate date;

    @Column(length = 500)
    private String description;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(nullable = false)
    private boolean imported = false;
}
