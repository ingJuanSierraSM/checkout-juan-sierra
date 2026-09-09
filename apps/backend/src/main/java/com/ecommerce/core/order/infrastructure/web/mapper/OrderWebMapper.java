package com.ecommerce.core.order.infrastructure.web.mapper;

import com.ecommerce.core.order.domain.model.Order;
import com.ecommerce.core.order.domain.model.OrderDiscount;
import com.ecommerce.core.order.domain.model.OrderItem;
import com.ecommerce.core.order.infrastructure.web.dto.OrderDiscountResponse;
import com.ecommerce.core.order.infrastructure.web.dto.OrderItemResponse;
import com.ecommerce.core.order.infrastructure.web.dto.OrderResponse;
import org.springframework.stereotype.Component;

@Component
public class OrderWebMapper {

    public OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.id(),
                order.originalSubtotal().amount(),
                order.calculatedDiscountBeforeCap().amount(),
                order.totalDiscount().amount(),
                order.effectiveDiscountPercentage(),
                order.finalTotal().amount(),
                order.discountCapApplied(),
                order.createdAt(),
                order.items().stream().map(this::toItemResponse).toList(),
                order.discounts().stream().map(this::toDiscountResponse).toList()
        );
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.productId(),
                item.productName(),
                item.unitPrice().amount(),
                item.category(),
                item.quantity()
        );
    }

    private OrderDiscountResponse toDiscountResponse(OrderDiscount discount) {
        return new OrderDiscountResponse(
                discount.type(),
                discount.name(),
                discount.percentage(),
                discount.amount().amount(),
                discount.sequence()
        );
    }
}
