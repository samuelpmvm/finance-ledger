package com.fintech.finance.ledger.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

@Entity
@Table(name = "budgets",
        indexes = {
                @Index(name = "idx_tx_tenant", columnList = "tenant_id"),
        })
@Getter @Setter @NoArgsConstructor
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private BigDecimal limitAmount;

    @Column(nullable = false)
    private YearMonth month; // Multi-month support

}
