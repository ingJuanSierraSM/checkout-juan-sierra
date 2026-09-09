package com.ecommerce.core.order.application.service;

import com.ecommerce.core.checkout.domain.model.Money;
import com.ecommerce.core.order.application.port.out.OrderRepository;
import com.ecommerce.core.order.domain.model.Order;
import com.ecommerce.core.order.domain.model.OrderItem;
import com.ecommerce.core.shared.application.exception.OrderNotFoundException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetOrderServiceTest {

    @Test
    void shouldReturnOrderWhenItExists() {
        Order order = sampleOrder(8L);
        OrderRepository repository = new OrderRepository() {
            @Override
            public Order save(Order orderToSave) {
                return orderToSave;
            }

            @Override
            public List<Order> findAll() {
                return List.of(order);
            }

            @Override
            public Optional<Order> findById(Long orderId) {
                return order.id().equals(orderId) ? Optional.of(order) : Optional.empty();
            }
        };

        assertThat(new GetOrderService(repository).getById(8L)).isEqualTo(order);
    }

    @Test
    void shouldRejectMissingOrder() {
        OrderRepository repository = new OrderRepository() {
            @Override
            public Order save(Order order) {
                return order;
            }

            @Override
            public List<Order> findAll() {
                return List.of();
            }

            @Override
            public Optional<Order> findById(Long orderId) {
                return Optional.empty();
            }
        };

        assertThatThrownBy(() -> new GetOrderService(repository).getById(999L))
                .isInstanceOf(OrderNotFoundException.class);
    }

    private Order sampleOrder(Long id) {
        return new Order(
                id,
                Money.of(new BigDecimal("10.00")),
                Money.zero(),
                Money.zero(),
                BigDecimal.ZERO,
                Money.of(new BigDecimal("10.00")),
                false,
                Instant.parse("2026-09-08T00:00:00Z"),
                List.of(new OrderItem(1L, "Producto", Money.of(new BigDecimal("10.00")),
                        com.ecommerce.core.catalog.domain.model.Category.HOME, 1)),
                List.of()
        );
    }
}
