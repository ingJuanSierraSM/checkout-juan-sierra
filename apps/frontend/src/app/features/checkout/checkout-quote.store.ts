import { HttpErrorResponse } from '@angular/common/http';
import { DestroyRef, Injectable, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { EMPTY, Subject, catchError, switchMap, tap } from 'rxjs';
import { ApiError } from '../../core/api/api-error.model';
import { CartStore } from '../cart/cart.store';
import { CheckoutApiService } from './checkout-api.service';
import { CheckoutQuote, CheckoutQuoteRequest, CheckoutUiError } from './checkout.model';

type QuoteStatus = 'idle' | 'loading' | 'ready' | 'error';

function isApiError(value: unknown): value is ApiError {
  return (
    typeof value === 'object' &&
    value !== null &&
    'code' in value &&
    'message' in value &&
    typeof value.code === 'string' &&
    typeof value.message === 'string'
  );
}

@Injectable({ providedIn: 'root' })
export class CheckoutQuoteStore {
  readonly #checkoutApi = inject(CheckoutApiService);
  readonly #cart = inject(CartStore);
  readonly #destroyRef = inject(DestroyRef);
  readonly #quoteRequests = new Subject<CheckoutQuoteRequest>();

  readonly quote = signal<CheckoutQuote | null>(null);
  readonly status = signal<QuoteStatus>('idle');
  readonly error = signal<CheckoutUiError | null>(null);
  readonly appliedCoupon = signal<string | null>(null);

  constructor() {
    this.#quoteRequests
      .pipe(
        switchMap((request) =>
          this.#checkoutApi.quote(request).pipe(
            tap((quote) => {
              this.quote.set(quote);
              this.error.set(null);
              this.status.set('ready');
            }),
            catchError((httpError: unknown) => {
              this.quote.set(null);
              this.error.set(this.toUiError(httpError));
              this.status.set('error');
              return EMPTY;
            }),
          ),
        ),
        takeUntilDestroyed(this.#destroyRef),
      )
      .subscribe();
  }

  refreshQuote(): void {
    if (this.#cart.isEmpty()) {
      this.reset();
      return;
    }

    this.status.set('loading');
    this.error.set(null);
    this.#quoteRequests.next({
      items: this.#cart.items().map((item) => ({
        productId: item.product.id,
        quantity: item.quantity,
      })),
      couponCode: this.appliedCoupon(),
    });
  }

  applyCoupon(couponCode: string): void {
    const normalizedCoupon = couponCode.trim().toUpperCase();
    this.appliedCoupon.set(normalizedCoupon || null);
    this.refreshQuote();
  }

  removeCoupon(): void {
    this.appliedCoupon.set(null);
    this.refreshQuote();
  }

  reset(): void {
    this.quote.set(null);
    this.error.set(null);
    this.status.set('idle');
  }

  private toUiError(httpError: unknown): CheckoutUiError {
    if (httpError instanceof HttpErrorResponse && isApiError(httpError.error)) {
      return { code: httpError.error.code, message: httpError.error.message };
    }

    return {
      code: 'QUOTE_UNAVAILABLE',
      message: 'No pudimos calcular los descuentos. Intenta nuevamente.',
    };
  }
}
