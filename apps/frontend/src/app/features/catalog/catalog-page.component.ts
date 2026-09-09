import { CurrencyPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { CartStore } from '../cart/cart.store';
import { CheckoutProcessStore } from '../checkout/checkout-process.store';
import { CheckoutQuoteStore } from '../checkout/checkout-quote.store';
import { ProductApiService } from './product-api.service';
import { ProductCardComponent } from './product-card.component';
import { Product } from './product.model';

type CatalogStatus = 'loading' | 'ready' | 'error';

@Component({
  selector: 'app-catalog-page',
  imports: [CurrencyPipe, RouterLink, ProductCardComponent],
  templateUrl: './catalog-page.component.html',
  styleUrl: './catalog-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CatalogPageComponent {
  readonly #productsApi = inject(ProductApiService);
  readonly #destroyRef = inject(DestroyRef);
  readonly cart = inject(CartStore);
  readonly checkout = inject(CheckoutQuoteStore);
  readonly checkoutProcess = inject(CheckoutProcessStore);

  readonly products = signal<readonly Product[]>([]);
  readonly status = signal<CatalogStatus>('loading');
  readonly productCount = computed(() => this.products().length);
  readonly displayedTotal = computed(
    () => this.checkout.quote()?.finalTotal ?? this.cart.originalSubtotal(),
  );
  readonly mobileCartOpen = signal(false);
  readonly couponCode = signal('');

  constructor() {
    this.loadProducts();
  }

  loadProducts(): void {
    this.status.set('loading');

    this.#productsApi
      .getProducts()
      .pipe(takeUntilDestroyed(this.#destroyRef))
      .subscribe({
        next: (products) => {
          this.products.set(products);
          this.status.set('ready');
        },
        error: () => {
          this.products.set([]);
          this.status.set('error');
        },
      });
  }

  addToCart(product: Product): void {
    if (this.checkoutProcess.isProcessing()) {
      return;
    }

    if (this.cart.add(product)) {
      this.checkout.refreshQuote();
    }
  }

  decreaseItem(productId: number): void {
    if (this.checkoutProcess.isProcessing()) {
      return;
    }

    this.cart.decrease(productId);
    if (this.cart.isEmpty()) {
      this.couponCode.set('');
    }
    this.checkout.refreshQuote();
  }

  removeItem(productId: number): void {
    if (this.checkoutProcess.isProcessing()) {
      return;
    }

    this.cart.remove(productId);
    if (this.cart.isEmpty()) {
      this.couponCode.set('');
    }
    this.checkout.refreshQuote();
  }

  applyCoupon(): void {
    if (this.checkoutProcess.isProcessing()) {
      return;
    }

    this.checkout.applyCoupon(this.couponCode());
  }

  updateCoupon(event: Event): void {
    const target = event.target;

    if (target instanceof HTMLInputElement) {
      this.couponCode.set(target.value);
    }
  }

  removeCoupon(): void {
    if (this.checkoutProcess.isProcessing()) {
      return;
    }

    this.couponCode.set('');
    this.checkout.removeCoupon();
  }

  confirmCheckout(): void {
    this.checkoutProcess.confirm();
  }

  openMobileCart(): void {
    this.mobileCartOpen.set(true);
  }

  closeMobileCart(): void {
    this.mobileCartOpen.set(false);
  }
}
