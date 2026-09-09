package com.ecommerce.core.order.application.service;

import com.ecommerce.core.order.application.port.in.GetOrderUseCase;
import com.ecommerce.core.order.application.port.out.OrderRepository;
import com.ecommerce.core.order.domain.model.Order;
import com.ecommerce.core.shared.application.exception.OrderNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class GetOrderService implements GetOrderUseCase {

    private final OrderRepository orderRepository;

    public GetOrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Order getById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}
