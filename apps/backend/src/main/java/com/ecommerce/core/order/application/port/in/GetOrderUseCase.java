package com.ecommerce.core.order.application.port.in;

import com.ecommerce.core.order.domain.model.Order;

public interface GetOrderUseCase {

    Order getById(Long orderId);
}
