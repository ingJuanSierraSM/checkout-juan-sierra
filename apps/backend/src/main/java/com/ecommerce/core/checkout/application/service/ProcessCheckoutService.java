package com.ecommerce.core.checkout.application.service;

import com.ecommerce.core.catalog.application.port.out.ProductRepository;
import com.ecommerce.core.checkout.application.model.CheckoutQuote;
import com.ecommerce.core.checkout.application.model.PreparedCheckout;
import com.ecommerce.core.checkout.application.model.ProcessedCheckout;
import com.ecommerce.core.checkout.application.model.QuoteCheckoutCommand;
import com.ecommerce.core.checkout.application.port.in.ProcessCheckoutUseCase;
import com.ecommerce.core.order.application.port.out.OrderRepository;
import com.ecommerce.core.order.domain.model.Order;
import com.ecommerce.core.order.domain.model.OrderDiscount;
import com.ecommerce.core.order.domain.model.OrderItem;
import com.ecommerce.core.promotion.application.port.out.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcessCheckoutService implements ProcessCheckoutUseCase {

    private final CheckoutPricingService checkoutPricingService;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final OrderRepository orderRepository;

    public ProcessCheckoutService(
            CheckoutPricingService checkoutPricingService,
            ProductRepository productRepository,
            CouponRepository couponRepository,
            OrderRepository orderRepository
    ) {
        this.checkoutPricingService = checkoutPricingService;
        this.productRepository = productRepository;
        this.couponRepository = couponRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public ProcessedCheckout process(QuoteCheckoutCommand command) {
        PreparedCheckout preparedCheckout = checkoutPricingService.prepareCheckout(command);

        preparedCheckout.items().forEach(item ->
                productRepository.save(item.product().decreaseStock(item.quantity()))
        );
        preparedCheckout.coupon().ifPresent(coupon ->
                couponRepository.save(coupon.markAsUsed(preparedCheckout.calculatedAt()))
        );

        Order order = Order.create(
                preparedCheckout.breakdown(),
                preparedCheckout.calculatedAt(),
                preparedCheckout.items().stream()
                        .map(item -> new OrderItem(
                                item.product().id(),
                                item.product().name(),
                                item.toPricingItem().unitPrice(),
                                item.product().category(),
                                item.quantity()
                        ))
                        .toList(),
                preparedCheckout.breakdown().discounts().stream()
                        .map(detail -> new OrderDiscount(
                                detail.type(),
                                detail.name(),
                                detail.percentage(),
                                detail.amount(),
                                detail.sequence()
                        ))
                        .toList()
        );
        Order savedOrder = orderRepository.save(order);
        return new ProcessedCheckout(
                savedOrder.id(),
                savedOrder.createdAt(),
                new CheckoutQuote(preparedCheckout.breakdown())
        );
    }
}
