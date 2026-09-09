package com.ecommerce.core.promotion.infrastructure.persistence;

import com.ecommerce.core.promotion.domain.model.DiscountPolicy;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "discount_policies")
public class DiscountPolicyJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(nullable = false, precision = 7, scale = 2)
    private BigDecimal value;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected DiscountPolicyJpaEntity() {
    }

    public DiscountPolicy toDomain() {
        return new DiscountPolicy(id, code, value, updatedAt);
    }
}
