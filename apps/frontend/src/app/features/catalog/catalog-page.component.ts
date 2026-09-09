import { CurrencyPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { CartStore } from '../cart/cart.store';
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

  readonly products = signal<readonly Product[]>([]);
  readonly status = signal<CatalogStatus>('loading');
  readonly productCount = computed(() => this.products().length);
  readonly mobileCartOpen = signal(false);

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
    this.cart.add(product);
  }

  openMobileCart(): void {
    this.mobileCartOpen.set(true);
  }

  closeMobileCart(): void {
    this.mobileCartOpen.set(false);
  }
}
