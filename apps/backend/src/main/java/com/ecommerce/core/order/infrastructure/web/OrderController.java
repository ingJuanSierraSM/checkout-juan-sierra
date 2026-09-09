package com.ecommerce.core.order.infrastructure.web;

import com.ecommerce.core.order.application.port.in.GetOrderUseCase;
import com.ecommerce.core.order.application.port.in.ListOrdersUseCase;
import com.ecommerce.core.order.infrastructure.web.dto.OrderResponse;
import com.ecommerce.core.order.infrastructure.web.mapper.OrderWebMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final ListOrdersUseCase listOrdersUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final OrderWebMapper orderWebMapper;

    public OrderController(
            ListOrdersUseCase listOrdersUseCase,
            GetOrderUseCase getOrderUseCase,
            OrderWebMapper orderWebMapper
    ) {
        this.listOrdersUseCase = listOrdersUseCase;
        this.getOrderUseCase = getOrderUseCase;
        this.orderWebMapper = orderWebMapper;
    }

    @GetMapping
    public List<OrderResponse> list() {
        return listOrdersUseCase.list().stream().map(orderWebMapper::toResponse).toList();
    }

    @GetMapping("/{orderId}")
    public OrderResponse getById(@PathVariable Long orderId) {
        return orderWebMapper.toResponse(getOrderUseCase.getById(orderId));
    }
}
