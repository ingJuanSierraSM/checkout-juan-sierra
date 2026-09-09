package com.ecommerce.core.order.infrastructure.persistence;

import com.ecommerce.core.order.application.port.out.OrderRepository;
import com.ecommerce.core.order.domain.model.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OrderRepositoryAdapter implements OrderRepository {

    private final SpringDataOrderRepository repository;

    public OrderRepositoryAdapter(SpringDataOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public Order save(Order order) {
        return repository.save(OrderJpaEntity.fromDomain(order)).toDomain();
    }

    @Override
    public List<Order> findAll() {
        return repository.findAllByOrderByCreatedAtDesc().stream().map(OrderJpaEntity::toDomain).toList();
    }

    @Override
    public Optional<Order> findById(Long orderId) {
        return repository.findDetailedById(orderId).map(OrderJpaEntity::toDomain);
    }
}
