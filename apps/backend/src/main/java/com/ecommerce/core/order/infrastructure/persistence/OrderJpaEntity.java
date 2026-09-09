package com.ecommerce.core.order.infrastructure.persistence;

import com.ecommerce.core.checkout.domain.model.Money;
import com.ecommerce.core.order.domain.model.Order;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "orders")
public class OrderJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal originalSubtotal;

    @Column(name = "calculated_discount_before_cap", nullable = false, precision = 12, scale = 2)
    private BigDecimal calculatedDiscountBeforeCap;

    @Column(name = "total_discount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalDiscount;

    @Column(name = "effective_discount_percentage", nullable = false, precision = 7, scale = 2)
    private BigDecimal effectiveDiscountPercentage;

    @Column(name = "final_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal finalTotal;

    @Column(name = "discount_cap_applied", nullable = false)
    private boolean discountCapApplied;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private Set<OrderItemJpaEntity> items = new LinkedHashSet<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequence ASC")
    private Set<OrderDiscountJpaEntity> discounts = new LinkedHashSet<>();

    protected OrderJpaEntity() {
    }

    public static OrderJpaEntity fromDomain(Order order) {
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.id = order.id();
        entity.originalSubtotal = order.originalSubtotal().amount();
        entity.calculatedDiscountBeforeCap = order.calculatedDiscountBeforeCap().amount();
        entity.totalDiscount = order.totalDiscount().amount();
        entity.effectiveDiscountPercentage = order.effectiveDiscountPercentage();
        entity.finalTotal = order.finalTotal().amount();
        entity.discountCapApplied = order.discountCapApplied();
        entity.createdAt = order.createdAt();
        order.items().forEach(item -> entity.addItem(OrderItemJpaEntity.fromDomain(item)));
        order.discounts().forEach(discount -> entity.addDiscount(OrderDiscountJpaEntity.fromDomain(discount)));
        return entity;
    }

    public Order toDomain() {
        return new Order(
                id,
                Money.of(originalSubtotal),
                Money.of(calculatedDiscountBeforeCap),
                Money.of(totalDiscount),
                effectiveDiscountPercentage,
                Money.of(finalTotal),
                discountCapApplied,
                createdAt,
                items.stream().map(OrderItemJpaEntity::toDomain).toList(),
                discounts.stream().map(OrderDiscountJpaEntity::toDomain).toList()
        );
    }

    private void addItem(OrderItemJpaEntity item) {
        item.assignOrder(this);
        items.add(item);
    }

    private void addDiscount(OrderDiscountJpaEntity discount) {
        discount.assignOrder(this);
        discounts.add(discount);
    }
}
