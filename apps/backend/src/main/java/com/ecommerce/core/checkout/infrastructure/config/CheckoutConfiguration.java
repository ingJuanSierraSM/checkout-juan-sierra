package com.ecommerce.core.checkout.infrastructure.config;

import com.ecommerce.core.checkout.domain.discount.CategoryDiscountStrategy;
import com.ecommerce.core.checkout.domain.discount.CouponDiscountStrategy;
import com.ecommerce.core.checkout.domain.discount.DiscountEngine;
import com.ecommerce.core.checkout.domain.discount.VolumeDiscountStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.List;

@Configuration
public class CheckoutConfiguration {

    @Bean
    DiscountEngine discountEngine() {
        return new DiscountEngine(List.of(
                new CategoryDiscountStrategy(),
                new VolumeDiscountStrategy(),
                new CouponDiscountStrategy()
        ));
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
