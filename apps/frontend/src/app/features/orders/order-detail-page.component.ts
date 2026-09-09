import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { map } from 'rxjs';
import { getCategoryLabel } from '../catalog/product.model';
import { OrderApiService } from './order-api.service';
import { Order } from './order.model';

type OrderDetailStatus = 'loading' | 'ready' | 'error';

@Component({
  selector: 'app-order-detail-page',
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './order-detail-page.component.html',
  styleUrl: './order-detail-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OrderDetailPageComponent {
  readonly #ordersApi = inject(OrderApiService);
  readonly #route = inject(ActivatedRoute);
  readonly #destroyRef = inject(DestroyRef);
  readonly #orderId = signal<number | null>(null);

  readonly order = signal<Order | null>(null);
  readonly status = signal<OrderDetailStatus>('loading');
  readonly getCategoryLabel = getCategoryLabel;

  constructor() {
    this.#route.paramMap
      .pipe(
        map((params) => Number(params.get('orderId'))),
        takeUntilDestroyed(this.#destroyRef),
      )
      .subscribe((orderId) => {
        if (!Number.isSafeInteger(orderId) || orderId <= 0) {
          this.#orderId.set(null);
          this.order.set(null);
          this.status.set('error');
          return;
        }

        this.#orderId.set(orderId);
        this.loadOrder();
      });
  }

  loadOrder(): void {
    const orderId = this.#orderId();

    if (orderId === null) {
      this.status.set('error');
      return;
    }

    this.status.set('loading');

    this.#ordersApi
      .getById(orderId)
      .pipe(takeUntilDestroyed(this.#destroyRef))
      .subscribe({
        next: (order) => {
          this.order.set(order);
          this.status.set('ready');
        },
        error: () => {
          this.order.set(null);
          this.status.set('error');
        },
      });
  }
}
