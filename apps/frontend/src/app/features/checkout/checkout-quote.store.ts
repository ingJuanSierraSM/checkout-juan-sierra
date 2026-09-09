import { DestroyRef, Injectable, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { EMPTY, Subject, catchError, switchMap, tap } from 'rxjs';
import { CartStore } from '../cart/cart.store';
import { CheckoutApiService } from './checkout-api.service';
import { toCheckoutUiError } from './checkout-error';
import { CheckoutQuote, CheckoutQuoteRequest, CheckoutUiError } from './checkout.model';

type QuoteStatus = 'idle' | 'loading' | 'ready' | 'error';

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
              this.error.set(
                toCheckoutUiError(
                  httpError,
                  'QUOTE_UNAVAILABLE',
                  'No pudimos calcular los descuentos. Intenta nuevamente.',
                ),
              );
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
    this.appliedCoupon.set(null);
    this.status.set('idle');
  }
}
