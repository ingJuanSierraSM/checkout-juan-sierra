package com.ecommerce.core.checkout.application.service;

import com.ecommerce.core.catalog.application.port.out.ProductRepository;
import com.ecommerce.core.catalog.domain.model.Category;
import com.ecommerce.core.catalog.domain.model.Product;
import com.ecommerce.core.checkout.application.model.CheckoutItem;
import com.ecommerce.core.checkout.application.model.QuoteCheckoutCommand;
import com.ecommerce.core.checkout.domain.discount.CategoryDiscountStrategy;
import com.ecommerce.core.checkout.domain.discount.CouponDiscountStrategy;
import com.ecommerce.core.checkout.domain.discount.DiscountEngine;
import com.ecommerce.core.checkout.domain.discount.VolumeDiscountStrategy;
import com.ecommerce.core.promotion.application.port.out.CouponRepository;
import com.ecommerce.core.promotion.application.port.out.DiscountPolicyRepository;
import com.ecommerce.core.promotion.domain.model.Coupon;
import com.ecommerce.core.promotion.domain.model.DiscountPolicy;
import com.ecommerce.core.shared.application.exception.CouponExpiredException;
import com.ecommerce.core.shared.application.exception.CouponAlreadyUsedException;
import com.ecommerce.core.shared.application.exception.CouponInactiveException;
import com.ecommerce.core.shared.application.exception.CouponNotFoundException;
import com.ecommerce.core.shared.application.exception.EmptyCartException;
import com.ecommerce.core.shared.application.exception.InactiveProductException;
import com.ecommerce.core.shared.application.exception.InsufficientStockException;
import com.ecommerce.core.shared.application.exception.InvalidCartException;
import com.ecommerce.core.shared.application.exception.ProductNotFoundException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuoteCheckoutServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-08T12:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    @Test
    void shouldCalculateQuoteWithoutConsumingCoupon() {
        FakeCouponRepository couponRepository = new FakeCouponRepository(Optional.of(validCoupon()));
        QuoteCheckoutService service = service(List.of(laptop(5, true)), couponRepository);

        var quote = service.quote(new QuoteCheckoutCommand(
                List.of(new CheckoutItem(1L, 1)),
                " welcome2026 "
        ));

        assertThat(quote.breakdown().finalTotal().amount()).isEqualByComparingTo("87.21");
        assertThat(quote.breakdown().totalDiscount().amount()).isEqualByComparingTo("32.79");
        assertThat(couponRepository.saveInvocations).isZero();
    }

    @Test
    void shouldRejectEmptyCart() {
        QuoteCheckoutService service = service(List.of(laptop(5, true)), new FakeCouponRepository(Optional.empty()));

        assertThatThrownBy(() -> service.quote(new QuoteCheckoutCommand(List.of(), null)))
                .isInstanceOf(EmptyCartException.class);
    }

    @Test
    void shouldRejectDuplicatedProducts() {
        QuoteCheckoutService service = service(List.of(laptop(5, true)), new FakeCouponRepository(Optional.empty()));

        assertThatThrownBy(() -> service.quote(new QuoteCheckoutCommand(
                List.of(new CheckoutItem(1L, 1), new CheckoutItem(1L, 1)), null
        ))).isInstanceOf(InvalidCartException.class);
    }

    @Test
    void shouldRejectInsufficientStock() {
        QuoteCheckoutService service = service(List.of(laptop(1, true)), new FakeCouponRepository(Optional.empty()));

        assertThatThrownBy(() -> service.quote(new QuoteCheckoutCommand(List.of(new CheckoutItem(1L, 2)), null)))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void shouldRejectMissingProduct() {
        QuoteCheckoutService service = service(List.of(), new FakeCouponRepository(Optional.empty()));

        assertThatThrownBy(() -> service.quote(new QuoteCheckoutCommand(List.of(new CheckoutItem(99L, 1)), null)))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void shouldRejectInactiveProduct() {
        QuoteCheckoutService service = service(List.of(laptop(5, false)), new FakeCouponRepository(Optional.empty()));

        assertThatThrownBy(() -> service.quote(new QuoteCheckoutCommand(List.of(new CheckoutItem(1L, 1)), null)))
                .isInstanceOf(InactiveProductException.class);
    }

    @Test
    void shouldRejectExpiredCoupon() {
        Coupon expiredCoupon = new Coupon(1L, "WELCOME2026", new BigDecimal("15"), true, NOW.minusSeconds(1), null);
        QuoteCheckoutService service = service(List.of(laptop(5, true)), new FakeCouponRepository(Optional.of(expiredCoupon)));

        assertThatThrownBy(() -> service.quote(new QuoteCheckoutCommand(List.of(new CheckoutItem(1L, 1)), "WELCOME2026")))
                .isInstanceOf(CouponExpiredException.class);
    }

    @Test
    void shouldRejectUnregisteredCoupon() {
        QuoteCheckoutService service = service(List.of(laptop(5, true)), new FakeCouponRepository(Optional.empty()));

        assertThatThrownBy(() -> service.quote(new QuoteCheckoutCommand(
                List.of(new CheckoutItem(1L, 1)), "NOT_REGISTERED"
        ))).isInstanceOf(CouponNotFoundException.class);
    }

    @Test
    void shouldRejectUsedCoupon() {
        Coupon usedCoupon = new Coupon(1L, "WELCOME2026", new BigDecimal("15"), true,
                NOW.plusSeconds(3600), NOW.minusSeconds(1));
        QuoteCheckoutService service = service(List.of(laptop(5, true)), new FakeCouponRepository(Optional.of(usedCoupon)));

        assertThatThrownBy(() -> service.quote(new QuoteCheckoutCommand(List.of(new CheckoutItem(1L, 1)), "WELCOME2026")))
                .isInstanceOf(CouponAlreadyUsedException.class);
    }

    @Test
    void shouldRejectInactiveCoupon() {
        Coupon inactiveCoupon = new Coupon(1L, "WELCOME2026", new BigDecimal("15"), false,
                NOW.plusSeconds(3600), null);
        QuoteCheckoutService service = service(List.of(laptop(5, true)), new FakeCouponRepository(Optional.of(inactiveCoupon)));

        assertThatThrownBy(() -> service.quote(new QuoteCheckoutCommand(List.of(new CheckoutItem(1L, 1)), "WELCOME2026")))
                .isInstanceOf(CouponInactiveException.class);
    }

    private QuoteCheckoutService service(List<Product> products, CouponRepository couponRepository) {
        ProductRepository productRepository = new FakeProductRepository(products.stream()
                .collect(java.util.stream.Collectors.toMap(Product::id, product -> product)));
        DiscountPolicyRepository policyRepository = code -> Optional.of(new DiscountPolicy(
                1L,
                CheckoutPricingService.MAXIMUM_DISCOUNT_POLICY_CODE,
                new BigDecimal("35.00"),
                NOW
        ));
        DiscountEngine engine = new DiscountEngine(List.of(
                new CategoryDiscountStrategy(),
                new VolumeDiscountStrategy(),
                new CouponDiscountStrategy()
        ));
        return new QuoteCheckoutService(new CheckoutPricingService(
                productRepository,
                couponRepository,
                policyRepository,
                engine,
                CLOCK
        ));
    }

    private Product laptop(int stock, boolean active) {
        return new Product(1L, "Laptop Pro", new BigDecimal("120.00"), Category.TECHNOLOGY, stock, active,
                "/products/laptop-pro.webp");
    }

    private Coupon validCoupon() {
        return new Coupon(1L, "WELCOME2026", new BigDecimal("15.00"), true, NOW.plusSeconds(3600), null);
    }

    private static final class FakeProductRepository implements ProductRepository {
        private final Map<Long, Product> products;

        private FakeProductRepository(Map<Long, Product> products) {
            this.products = products;
        }

        @Override
        public List<Product> findActiveProducts() {
            return products.values().stream().filter(Product::active).toList();
        }

        @Override
        public Optional<Product> findById(Long id) {
            return Optional.ofNullable(products.get(id));
        }

        @Override
        public Optional<Product> findByIdForUpdate(Long id) {
            return findById(id);
        }

        @Override
        public Product save(Product product) {
            products.put(product.id(), product);
            return product;
        }
    }

    private static final class FakeCouponRepository implements CouponRepository {
        private final Optional<Coupon> coupon;
        private int saveInvocations;

        private FakeCouponRepository(Optional<Coupon> coupon) {
            this.coupon = coupon;
        }

        @Override
        public Optional<Coupon> findByCode(String code) {
            return coupon.filter(value -> value.code().equals(code));
        }

        @Override
        public Optional<Coupon> findByCodeForUpdate(String code) {
            return findByCode(code);
        }

        @Override
        public Coupon save(Coupon coupon) {
            saveInvocations++;
            return coupon;
        }
    }
}
