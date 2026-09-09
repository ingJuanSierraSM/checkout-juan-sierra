package com.ecommerce.core.order.infrastructure.persistence;

import com.ecommerce.core.catalog.domain.model.Category;
import com.ecommerce.core.checkout.domain.model.Money;
import com.ecommerce.core.order.domain.model.OrderItem;
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
@Table(name = "order_items")
public class OrderItemJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderJpaEntity order;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 150)
    private String productName;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Category category;

    @Column(nullable = false)
    private int quantity;

    protected OrderItemJpaEntity() {
    }

    public static OrderItemJpaEntity fromDomain(OrderItem item) {
        OrderItemJpaEntity entity = new OrderItemJpaEntity();
        entity.productId = item.productId();
        entity.productName = item.productName();
        entity.unitPrice = item.unitPrice().amount();
        entity.category = item.category();
        entity.quantity = item.quantity();
        return entity;
    }

    public void assignOrder(OrderJpaEntity order) {
        this.order = order;
    }

    public OrderItem toDomain() {
        return new OrderItem(productId, productName, Money.of(unitPrice), category, quantity);
    }
}
