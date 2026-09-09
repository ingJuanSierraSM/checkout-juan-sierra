package com.ecommerce.core.order.application.port.in;

import com.ecommerce.core.order.domain.model.Order;

import java.util.List;

public interface ListOrdersUseCase {

    List<Order> list();
}
