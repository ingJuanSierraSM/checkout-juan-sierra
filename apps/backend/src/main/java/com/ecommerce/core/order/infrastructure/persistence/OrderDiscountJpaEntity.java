package com.ecommerce.core.order.infrastructure.persistence;

import com.ecommerce.core.checkout.domain.model.DiscountType;
import com.ecommerce.core.checkout.domain.model.Money;
import com.ecommerce.core.order.domain.model.OrderDiscount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "order_discounts")
public class OrderDiscountJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderJpaEntity order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DiscountType type;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, precision = 7, scale = 2)
    private BigDecimal percentage;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private int sequence;

    protected OrderDiscountJpaEntity() {
    }

    public static OrderDiscountJpaEntity fromDomain(OrderDiscount discount) {
        OrderDiscountJpaEntity entity = new OrderDiscountJpaEntity();
        entity.type = discount.type();
        entity.name = discount.name();
        entity.percentage = discount.percentage();
        entity.amount = discount.amount().amount();
        entity.sequence = discount.sequence();
        return entity;
    }

    public void assignOrder(OrderJpaEntity order) {
        this.order = order;
    }

    public OrderDiscount toDomain() {
        return new OrderDiscount(type, name, percentage, Money.of(amount), sequence);
    }
}
