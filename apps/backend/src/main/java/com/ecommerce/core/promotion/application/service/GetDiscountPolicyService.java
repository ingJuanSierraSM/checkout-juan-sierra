package com.ecommerce.core.promotion.application.service;

import com.ecommerce.core.promotion.application.port.out.DiscountPolicyRepository;
import com.ecommerce.core.promotion.domain.model.DiscountPolicy;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetDiscountPolicyService {

    private final DiscountPolicyRepository discountPolicyRepository;

    public GetDiscountPolicyService(DiscountPolicyRepository discountPolicyRepository) {
        this.discountPolicyRepository = discountPolicyRepository;
    }

    public Optional<DiscountPolicy> findByCode(String code) {
        return discountPolicyRepository.findByCode(code);
    }
}
