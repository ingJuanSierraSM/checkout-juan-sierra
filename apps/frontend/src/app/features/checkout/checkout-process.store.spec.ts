import { HttpErrorResponse } from '@angular/common/http';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { Observable, Subject, of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { CartStore } from '../cart/cart.store';
import { Product } from '../catalog/product.model';
import { CheckoutApiService } from './checkout-api.service';
import { CheckoutProcessStore } from './checkout-process.store';
import { CheckoutCompleted, CheckoutQuoteRequest } from './checkout.model';
import { CheckoutQuoteStore } from './checkout-quote.store';

describe('CheckoutProcessStore', () => {
  const laptop: Product = {
    id: 1,
    name: 'Laptop Pro',
    unitPrice: 120,
    category: 'TECHNOLOGY',
    stock: 5,
    imageUrl: '/products/laptop-pro.webp',
  };
  const completion: CheckoutCompleted = {
    orderId: 42,
    createdAt: '2026-09-09T12:00:00Z',
    originalSubtotal: 120,
    discounts: [{ type: 'CATEGORY', name: 'Descuento Tecnología', percentage: 10, amount: 12, sequence: 1 }],
    calculatedDiscountBeforeCap: 12,
    totalDiscount: 12,
    effectiveDiscountPercentage: 10,
    finalTotal: 108,
    discountCapApplied: false,
    maximumDiscountPercentage: 35,
  };

  let cart: CartStore;
  let store: CheckoutProcessStore;
  let processCheckout: ReturnType<typeof vi.fn<(request: CheckoutQuoteRequest) => Observable<CheckoutCompleted>>>;
  const quoteStore = {
    status: signal<'idle' | 'loading' | 'ready' | 'error'>('ready'),
    appliedCoupon: signal<string | null>(null),
    reset: vi.fn(),
  };
  const router = { navigate: vi.fn().mockResolvedValue(true) };

  beforeEach(() => {
    processCheckout = vi.fn<(request: CheckoutQuoteRequest) => Observable<CheckoutCompleted>>();

    TestBed.configureTestingModule({
      providers: [
        CartStore,
        CheckoutProcessStore,
        { provide: CheckoutApiService, useValue: { process: processCheckout } },
        { provide: CheckoutQuoteStore, useValue: quoteStore },
        { provide: Router, useValue: router },
      ],
    });

    cart = TestBed.inject(CartStore);
    store = TestBed.inject(CheckoutProcessStore);
    cart.clear();
    quoteStore.status.set('ready');
    quoteStore.appliedCoupon.set(null);
    quoteStore.reset.mockReset();
    router.navigate.mockReset();
    router.navigate.mockResolvedValue(true);
  });

  it('confirms the quoted cart, clears it and opens the success view', () => {
    processCheckout.mockReturnValue(of(completion));
    cart.add(laptop);
    quoteStore.appliedCoupon.set('WELCOME2026');

    store.confirm();

    expect(processCheckout).toHaveBeenCalledWith({
      items: [{ productId: 1, quantity: 1 }],
      couponCode: 'WELCOME2026',
    });
    expect(store.completion()).toEqual(completion);
    expect(store.status()).toBe('success');
    expect(cart.isEmpty()).toBe(true);
    expect(quoteStore.reset).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/checkout/success']);
  });

  it('prevents duplicate confirmation while a request is in progress', () => {
    const response = new Subject<CheckoutCompleted>();
    processCheckout.mockReturnValue(response);
    cart.add(laptop);

    store.confirm();
    store.confirm();

    expect(processCheckout).toHaveBeenCalledTimes(1);
    expect(store.isProcessing()).toBe(true);
  });

  it('keeps the cart and exposes the backend business error when confirmation fails', () => {
    processCheckout.mockReturnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 409,
            error: { code: 'INSUFFICIENT_STOCK', message: 'Stock insuficiente para Laptop Pro' },
          }),
      ),
    );
    cart.add(laptop);

    store.confirm();

    expect(store.status()).toBe('error');
    expect(store.error()).toEqual({
      code: 'INSUFFICIENT_STOCK',
      message: 'Stock insuficiente para Laptop Pro',
    });
    expect(cart.isEmpty()).toBe(false);
  });

  it('does not call the endpoint without a current quote or cart', () => {
    store.confirm();
    quoteStore.status.set('idle');
    cart.add(laptop);
    store.confirm();

    expect(processCheckout).not.toHaveBeenCalled();
  });
});
