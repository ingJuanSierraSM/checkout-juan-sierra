import { DestroyRef, Injectable, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { CartStore } from '../cart/cart.store';
import { CheckoutApiService } from './checkout-api.service';
import { toCheckoutUiError } from './checkout-error';
import { CheckoutCompleted, CheckoutQuoteRequest, CheckoutUiError } from './checkout.model';
import { CheckoutQuoteStore } from './checkout-quote.store';

type CheckoutProcessStatus = 'idle' | 'processing' | 'success' | 'error';

@Injectable({ providedIn: 'root' })
export class CheckoutProcessStore {
  readonly #checkoutApi = inject(CheckoutApiService);
  readonly #cart = inject(CartStore);
  readonly #quote = inject(CheckoutQuoteStore);
  readonly #router = inject(Router);
  readonly #destroyRef = inject(DestroyRef);

  readonly status = signal<CheckoutProcessStatus>('idle');
  readonly completion = signal<CheckoutCompleted | null>(null);
  readonly error = signal<CheckoutUiError | null>(null);
  readonly isProcessing = computed(() => this.status() === 'processing');

  confirm(): void {
    if (this.#cart.isEmpty() || this.#quote.status() !== 'ready' || this.isProcessing()) {
      return;
    }

    const request: CheckoutQuoteRequest = {
      items: this.#cart.items().map((item) => ({
        productId: item.product.id,
        quantity: item.quantity,
      })),
      couponCode: this.#quote.appliedCoupon(),
    };

    this.status.set('processing');
    this.error.set(null);

    this.#checkoutApi
      .process(request)
      .pipe(takeUntilDestroyed(this.#destroyRef))
      .subscribe({
        next: (completion) => {
          this.completion.set(completion);
          this.status.set('success');
          this.#cart.clear();
          this.#quote.reset();
          void this.#router.navigate(['/checkout/success']);
        },
        error: (httpError: unknown) => {
          this.error.set(
            toCheckoutUiError(
              httpError,
              'CHECKOUT_UNAVAILABLE',
              'No pudimos confirmar tu compra. Intenta nuevamente.',
            ),
          );
          this.status.set('error');
        },
      });
  }

  reset(): void {
    this.status.set('idle');
    this.completion.set(null);
    this.error.set(null);
  }
}
