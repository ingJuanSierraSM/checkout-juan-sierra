package com.ecommerce.core.checkout.application.service;

import com.ecommerce.core.catalog.application.port.out.ProductRepository;
import com.ecommerce.core.catalog.domain.model.Product;
import com.ecommerce.core.checkout.application.model.CheckoutItem;
import com.ecommerce.core.checkout.application.model.PreparedCheckout;
import com.ecommerce.core.checkout.application.model.QuoteCheckoutCommand;
import com.ecommerce.core.checkout.application.model.ResolvedCheckoutItem;
import com.ecommerce.core.checkout.domain.discount.DiscountEngine;
import com.ecommerce.core.checkout.domain.discount.MaximumDiscountPolicy;
import com.ecommerce.core.checkout.domain.model.DiscountContext;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
public class CheckoutPricingService {

    public static final String MAXIMUM_DISCOUNT_POLICY_CODE = "MAX_TOTAL_DISCOUNT_PERCENTAGE";

    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final DiscountPolicyRepository discountPolicyRepository;
    private final DiscountEngine discountEngine;
    private final Clock clock;

    public CheckoutPricingService(
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

    public PreparedCheckout prepareQuote(QuoteCheckoutCommand command) {
        return prepare(command, false);
    }

    public PreparedCheckout prepareCheckout(QuoteCheckoutCommand command) {
        return prepare(command, true);
    }

    private PreparedCheckout prepare(QuoteCheckoutCommand command, boolean lockResources) {
        Instant now = clock.instant();
        List<ResolvedCheckoutItem> items = resolveItems(command, lockResources);
        Optional<Coupon> coupon = resolveCoupon(command == null ? null : command.couponCode(), now, lockResources);
        var policy = discountPolicyRepository.findByCode(MAXIMUM_DISCOUNT_POLICY_CODE)
                .orElseThrow(() -> new DiscountPolicyNotFoundException(MAXIMUM_DISCOUNT_POLICY_CODE));

        var pricingItems = items.stream().map(ResolvedCheckoutItem::toPricingItem).toList();
        var context = new DiscountContext(pricingItems, coupon, now);
        var maximumDiscountPolicy = new MaximumDiscountPolicy(policy.value());
        return new PreparedCheckout(items, coupon, discountEngine.calculate(context, maximumDiscountPolicy), now);
    }

    private List<ResolvedCheckoutItem> resolveItems(QuoteCheckoutCommand command, boolean lockResources) {
        if (command == null || command.items() == null || command.items().isEmpty()) {
            throw new EmptyCartException();
        }

        Set<Long> productIds = new HashSet<>();
        return command.items().stream().map(item -> resolveItem(item, productIds, lockResources)).toList();
    }

    private ResolvedCheckoutItem resolveItem(CheckoutItem item, Set<Long> productIds, boolean lockResources) {
        if (item == null || item.productId() == null || item.quantity() == null || item.quantity() <= 0) {
            throw new InvalidCartException("Cada producto debe incluir id y cantidad mayor que cero");
        }
        if (!productIds.add(item.productId())) {
            throw new InvalidCartException("El producto con id " + item.productId() + " está repetido en el carrito");
        }

        Optional<Product> productResult = lockResources
                ? productRepository.findByIdForUpdate(item.productId())
                : productRepository.findById(item.productId());
        Product product = productResult.orElseThrow(() -> new ProductNotFoundException(item.productId()));
        if (!product.active()) {
            throw new InactiveProductException(product.id());
        }
        if (product.stock() < item.quantity()) {
            throw new InsufficientStockException(product.id(), product.stock());
        }
        return new ResolvedCheckoutItem(product, item.quantity());
    }

    private Optional<Coupon> resolveCoupon(String couponCode, Instant now, boolean lockResources) {
        if (couponCode == null || couponCode.isBlank()) {
            return Optional.empty();
        }

        String normalizedCode = couponCode.trim().toUpperCase(Locale.ROOT);
        Optional<Coupon> couponResult = lockResources
                ? couponRepository.findByCodeForUpdate(normalizedCode)
                : couponRepository.findByCode(normalizedCode);
        Coupon coupon = couponResult.orElseThrow(() -> new CouponNotFoundException(normalizedCode));
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
