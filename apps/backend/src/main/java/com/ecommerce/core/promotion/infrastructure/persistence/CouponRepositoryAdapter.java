package com.ecommerce.core.promotion.infrastructure.persistence;

import com.ecommerce.core.promotion.application.port.out.CouponRepository;
import com.ecommerce.core.promotion.domain.model.Coupon;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CouponRepositoryAdapter implements CouponRepository {

    private final SpringDataCouponRepository repository;

    public CouponRepositoryAdapter(SpringDataCouponRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Coupon> findByCode(String code) {
        return repository.findByCode(code).map(CouponJpaEntity::toDomain);
    }

    @Override
    public Optional<Coupon> findByCodeForUpdate(String code) {
        return repository.findByCodeForUpdate(code).map(CouponJpaEntity::toDomain);
    }

    @Override
    public Coupon save(Coupon coupon) {
        return repository.save(CouponJpaEntity.fromDomain(coupon)).toDomain();
    }
}
