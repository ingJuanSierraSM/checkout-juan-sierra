package com.ecommerce.core.order.application.port.out;

import com.ecommerce.core.order.domain.model.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    List<Order> findAll();

    Optional<Order> findById(Long orderId);
}
