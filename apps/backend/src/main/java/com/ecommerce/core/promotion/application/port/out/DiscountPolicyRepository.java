package com.ecommerce.core.promotion.application.port.out;

import com.ecommerce.core.promotion.domain.model.DiscountPolicy;

import java.util.Optional;

public interface DiscountPolicyRepository {

    Optional<DiscountPolicy> findByCode(String code);
}
