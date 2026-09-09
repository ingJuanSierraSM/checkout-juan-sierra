package com.ecommerce.core.promotion.infrastructure.persistence;

import com.ecommerce.core.promotion.domain.model.Coupon;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "coupons")
public class CouponJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String code;

    @Column(nullable = false, precision = 7, scale = 2)
    private BigDecimal percentage;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    protected CouponJpaEntity() {
    }

    public static CouponJpaEntity fromDomain(Coupon coupon) {
        CouponJpaEntity entity = new CouponJpaEntity();
        entity.id = coupon.id();
        entity.code = coupon.code();
        entity.percentage = coupon.percentage();
        entity.active = coupon.active();
        entity.expiresAt = coupon.expiresAt();
        entity.usedAt = coupon.usedAt();
        return entity;
    }

    public Coupon toDomain() {
        return new Coupon(id, code, percentage, active, expiresAt, usedAt);
    }
}
