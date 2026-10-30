package com.example.transaction_management.entity;

import com.example.transaction_management.common.id.PrefixedId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "transactions")
public class TransactionEntity {

    @Id
    @PrefixedId(prefix = "TRN_")
    @Column(length = 30)
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "category_id", nullable = false)
    private int categoryId;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "receiver", nullable = false)
    private String receiver;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (paidAt == null) paidAt = now;
        currency = (currency == null) ? "USD" : currency.trim().toUpperCase();
    }


}
