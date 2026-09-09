import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { Observable, Subject, of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { CartStore } from '../cart/cart.store';
import { Product } from '../catalog/product.model';
import { CheckoutApiService } from './checkout-api.service';
import { CheckoutQuoteStore } from './checkout-quote.store';
import { CheckoutQuote, CheckoutQuoteRequest } from './checkout.model';

describe('CheckoutQuoteStore', () => {
  const laptop: Product = {
    id: 1,
    name: 'Laptop Pro',
    unitPrice: 120,
    category: 'TECHNOLOGY',
    stock: 5,
    imageUrl: '/products/laptop-pro.webp',
  };
  const coffeeMaker: Product = {
    id: 5,
    name: 'Coffee Maker',
    unitPrice: 60,
    category: 'HOME',
    stock: 7,
    imageUrl: '/products/coffee-maker.webp',
  };
  const quote: CheckoutQuote = {
    originalSubtotal: 120,
    discounts: [
      { type: 'CATEGORY', name: 'Descuento de categoría', percentage: 10, amount: 12, sequence: 1 },
    ],
    calculatedDiscountBeforeCap: 12,
    totalDiscount: 12,
    effectiveDiscountPercentage: 10,
    finalTotal: 108,
    discountCapApplied: false,
    maximumDiscountPercentage: 35,
  };

  let cart: CartStore;
  let store: CheckoutQuoteStore;
  let requestQuote: ReturnType<typeof vi.fn<(request: CheckoutQuoteRequest) => Observable<CheckoutQuote>>>;

  beforeEach(() => {
    requestQuote = vi.fn<(request: CheckoutQuoteRequest) => Observable<CheckoutQuote>>();

    TestBed.configureTestingModule({
      providers: [
        CartStore,
        CheckoutQuoteStore,
        { provide: CheckoutApiService, useValue: { quote: requestQuote } },
      ],
    });

    cart = TestBed.inject(CartStore);
    store = TestBed.inject(CheckoutQuoteStore);
  });

  it('quotes the current cart with its applied coupon', () => {
    requestQuote.mockReturnValue(of(quote));
    cart.add(laptop);

    store.applyCoupon(' welcome2026 ');

    expect(requestQuote).toHaveBeenCalledWith({
      items: [{ productId: 1, quantity: 1 }],
      couponCode: 'WELCOME2026',
    });
    expect(store.appliedCoupon()).toBe('WELCOME2026');
    expect(store.quote()).toEqual(quote);
    expect(store.status()).toBe('ready');
  });

  it('clears the quote when the cart is empty and when the coupon is removed', () => {
    requestQuote.mockReturnValue(of(quote));
    cart.add(laptop);
    store.applyCoupon('WELCOME2026');

    store.removeCoupon();
    expect(requestQuote).toHaveBeenLastCalledWith({
      items: [{ productId: 1, quantity: 1 }],
      couponCode: null,
    });

    cart.clear();
    store.refreshQuote();
    expect(store.quote()).toBeNull();
    expect(store.status()).toBe('idle');
  });

  it('keeps only the most recent quote when requests complete out of order', () => {
    const firstResponse = new Subject<CheckoutQuote>();
    const secondResponse = new Subject<CheckoutQuote>();
    requestQuote.mockReturnValueOnce(firstResponse).mockReturnValueOnce(secondResponse);
    cart.add(laptop);
    store.refreshQuote();
    cart.add(coffeeMaker);
    store.refreshQuote();

    secondResponse.next({ ...quote, originalSubtotal: 180, finalTotal: 162 });
    firstResponse.next({ ...quote, originalSubtotal: 120, finalTotal: 108 });

    expect(store.quote()?.originalSubtotal).toBe(180);
    expect(store.quote()?.finalTotal).toBe(162);
  });

  it('exposes the business message returned by the backend', () => {
    requestQuote.mockReturnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 404,
            error: {
              code: 'COUPON_NOT_FOUND',
              message: 'El cupón no existe',
            },
          }),
      ),
    );
    cart.add(laptop);

    store.applyCoupon('INVALIDO');

    expect(store.status()).toBe('error');
    expect(store.quote()).toBeNull();
    expect(store.error()).toEqual({ code: 'COUPON_NOT_FOUND', message: 'El cupón no existe' });
  });

  it('uses a generic message when the quote service is unavailable', () => {
    requestQuote.mockReturnValue(throwError(() => new Error('Network unavailable')));
    cart.add(laptop);

    store.refreshQuote();

    expect(store.error()?.code).toBe('QUOTE_UNAVAILABLE');
  });
});
