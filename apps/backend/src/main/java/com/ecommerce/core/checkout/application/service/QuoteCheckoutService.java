package com.ecommerce.core.checkout.application.service;

import com.ecommerce.core.catalog.application.port.out.ProductRepository;
import com.ecommerce.core.catalog.domain.model.Product;
import com.ecommerce.core.checkout.application.model.CheckoutItem;
import com.ecommerce.core.checkout.application.model.CheckoutQuote;
import com.ecommerce.core.checkout.application.model.QuoteCheckoutCommand;
import com.ecommerce.core.checkout.application.port.in.QuoteCheckoutUseCase;
import com.ecommerce.core.checkout.domain.discount.DiscountEngine;
import com.ecommerce.core.checkout.domain.discount.MaximumDiscountPolicy;
import com.ecommerce.core.checkout.domain.model.DiscountContext;
import com.ecommerce.core.checkout.domain.model.Money;
import com.ecommerce.core.checkout.domain.model.PricingItem;
import com.ecommerce.core.promotion.application.port.out.CouponRepository;
import com.ecommerce.core.promotion.application.port.out.DiscountPolicyRepository;
import com.ecommerce.core.promotion.domain.model.Coupon;
import com.ecommerce.core.shared.application.exception.CouponAlreadyUsedException;
import com.ecommerce.core.shared.application.exception.CouponExpiredException;
import com.ecommerce.core.shared.application.exception.CouponInactiveException;
import com.ecommerce.core.shared.application.exception.CouponNotFoundException;
import com.ecommerce.core.shared.application.exception.DiscountPolicyNotFoundException;
import com.ecommerce.core.shared.application.exception.EmptyCartException;
import com.ecommerce.core.shared.application.exception.InactiveProductException;
import com.ecommerce.core.shared.application.exception.InsufficientStockException;
import com.ecommerce.core.shared.application.exception.InvalidCartException;
import com.ecommerce.core.shared.application.exception.ProductNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
public class QuoteCheckoutService implements QuoteCheckoutUseCase {

    public static final String MAXIMUM_DISCOUNT_POLICY_CODE = "MAX_TOTAL_DISCOUNT_PERCENTAGE";

    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final DiscountPolicyRepository discountPolicyRepository;
    private final DiscountEngine discountEngine;
    private final Clock clock;

    public QuoteCheckoutService(
            ProductRepository productRepository,
            CouponRepository couponRepository,
            DiscountPolicyRepository discountPolicyRepository,
            DiscountEngine discountEngine,
            Clock clock
    ) {
        this.productRepository = productRepository;
        this.couponRepository = couponRepository;
        this.discountPolicyRepository = discountPolicyRepository;
        this.discountEngine = discountEngine;
        this.clock = clock;
    }

    @Override
    public CheckoutQuote quote(QuoteCheckoutCommand command) {
        Instant now = clock.instant();
        List<PricingItem> pricingItems = resolveItems(command);
        Optional<Coupon> coupon = resolveCoupon(command.couponCode(), now);
        var policy = discountPolicyRepository.findByCode(MAXIMUM_DISCOUNT_POLICY_CODE)
                .orElseThrow(() -> new DiscountPolicyNotFoundException(MAXIMUM_DISCOUNT_POLICY_CODE));

        var context = new DiscountContext(pricingItems, coupon, now);
        var maximumDiscountPolicy = new MaximumDiscountPolicy(policy.value());
        return new CheckoutQuote(discountEngine.calculate(context, maximumDiscountPolicy));
    }

    private List<PricingItem> resolveItems(QuoteCheckoutCommand command) {
        if (command == null || command.items() == null || command.items().isEmpty()) {
            throw new EmptyCartException();
        }

        Set<Long> productIds = new HashSet<>();
        List<PricingItem> pricingItems = new ArrayList<>();
        for (CheckoutItem item : command.items()) {
            if (item == null || item.productId() == null || item.quantity() == null || item.quantity() <= 0) {
                throw new InvalidCartException("Cada producto debe incluir id y cantidad mayor que cero");
            }
            if (!productIds.add(item.productId())) {
                throw new InvalidCartException("El producto con id " + item.productId() + " está repetido en el carrito");
            }

            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new ProductNotFoundException(item.productId()));
            if (!product.active()) {
                throw new InactiveProductException(product.id());
            }
            if (product.stock() < item.quantity()) {
                throw new InsufficientStockException(product.id(), product.stock());
            }
            pricingItems.add(new PricingItem(
                    product.id(),
                    product.name(),
                    Money.of(product.unitPrice()),
                    product.category(),
                    item.quantity()
            ));
        }
        return pricingItems;
    }

    private Optional<Coupon> resolveCoupon(String couponCode, Instant now) {
        if (couponCode == null || couponCode.isBlank()) {
            return Optional.empty();
        }

        String normalizedCode = couponCode.trim().toUpperCase(Locale.ROOT);
        Coupon coupon = couponRepository.findByCode(normalizedCode)
                .orElseThrow(() -> new CouponNotFoundException(normalizedCode));
        if (!coupon.active()) {
            throw new CouponInactiveException(normalizedCode);
        }
        if (coupon.usedAt() != null) {
            throw new CouponAlreadyUsedException(normalizedCode);
        }
        if (!coupon.expiresAt().isAfter(now)) {
            throw new CouponExpiredException(normalizedCode);
        }
        return Optional.of(coupon);
    }
}
