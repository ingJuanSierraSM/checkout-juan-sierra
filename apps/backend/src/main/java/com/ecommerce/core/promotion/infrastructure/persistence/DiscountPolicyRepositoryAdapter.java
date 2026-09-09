package com.ecommerce.core.promotion.infrastructure.persistence;

import com.ecommerce.core.promotion.application.port.out.DiscountPolicyRepository;
import com.ecommerce.core.promotion.domain.model.DiscountPolicy;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DiscountPolicyRepositoryAdapter implements DiscountPolicyRepository {

    private final SpringDataDiscountPolicyRepository repository;

    public DiscountPolicyRepositoryAdapter(SpringDataDiscountPolicyRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<DiscountPolicy> findByCode(String code) {
        return repository.findByCode(code).map(DiscountPolicyJpaEntity::toDomain);
    }
}
