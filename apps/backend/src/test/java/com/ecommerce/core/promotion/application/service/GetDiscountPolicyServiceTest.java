package com.ecommerce.core.promotion.application.service;

import com.ecommerce.core.promotion.application.port.out.DiscountPolicyRepository;
import com.ecommerce.core.promotion.domain.model.DiscountPolicy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class GetDiscountPolicyServiceTest {

    @Test
    void shouldReturnConfiguredMaximumDiscountPolicy() {
        DiscountPolicy policy = new DiscountPolicy(
                1L,
                "MAX_TOTAL_DISCOUNT_PERCENTAGE",
                new BigDecimal("35.00"),
                Instant.parse("2026-09-08T12:00:00Z")
        );
        DiscountPolicyRepository repository = code -> Optional.of(policy);
        GetDiscountPolicyService service = new GetDiscountPolicyService(repository);

        Optional<DiscountPolicy> result = service.findByCode("MAX_TOTAL_DISCOUNT_PERCENTAGE");

        assertThat(result).containsSame(policy);
        assertThat(result.orElseThrow().value()).isEqualByComparingTo("35.00");
    }
}
