package com.ecommerce.core.order.application.service;

import com.ecommerce.core.order.application.port.in.ListOrdersUseCase;
import com.ecommerce.core.order.application.port.out.OrderRepository;
import com.ecommerce.core.order.domain.model.Order;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListOrdersService implements ListOrdersUseCase {

    private final OrderRepository orderRepository;

    public ListOrdersService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public List<Order> list() {
        return orderRepository.findAll();
    }
}
