package com.ecommerce.core.checkout.application.service;

import com.ecommerce.core.catalog.application.port.out.ProductRepository;
import com.ecommerce.core.checkout.application.model.CheckoutItem;
import com.ecommerce.core.checkout.application.model.QuoteCheckoutCommand;
import com.ecommerce.core.checkout.application.port.in.ProcessCheckoutUseCase;
import com.ecommerce.core.order.application.port.out.OrderRepository;
import com.ecommerce.core.promotion.application.port.out.CouponRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProcessCheckoutIntegrationTest {

    @Autowired
    private ProcessCheckoutUseCase processCheckoutUseCase;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void shouldPersistOrderConsumeCouponAndDecreaseStockAtomically() {
        int initialStock = productRepository.findById(1L).orElseThrow().stock();

        var result = processCheckoutUseCase.process(new QuoteCheckoutCommand(
                java.util.List.of(new CheckoutItem(1L, 1)),
                "WELCOME2026"
        ));

        assertThat(result.orderId()).isNotNull();
        assertThat(result.quote().breakdown().finalTotal().amount()).isEqualByComparingTo("87.21");
        assertThat(productRepository.findById(1L).orElseThrow().stock()).isEqualTo(initialStock - 1);
        assertThat(couponRepository.findByCode("WELCOME2026").orElseThrow().usedAt()).isNotNull();

        var savedOrder = orderRepository.findById(result.orderId()).orElseThrow();
        assertThat(savedOrder.items()).hasSize(1);
        assertThat(savedOrder.discounts()).hasSize(3);
        assertThat(savedOrder.finalTotal().amount()).isEqualByComparingTo("87.21");
    }
}
