import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { OrderApiService } from './order-api.service';
import { Order } from './order.model';

type OrdersStatus = 'loading' | 'ready' | 'error';

@Component({
  selector: 'app-orders-page',
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './orders-page.component.html',
  styleUrl: './orders-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OrdersPageComponent {
  readonly #ordersApi = inject(OrderApiService);
  readonly #destroyRef = inject(DestroyRef);

  readonly orders = signal<readonly Order[]>([]);
  readonly status = signal<OrdersStatus>('loading');
  readonly orderCount = computed(() => this.orders().length);

  constructor() {
    this.loadOrders();
  }

  loadOrders(): void {
    this.status.set('loading');

    this.#ordersApi
      .list()
      .pipe(takeUntilDestroyed(this.#destroyRef))
      .subscribe({
        next: (orders) => {
          this.orders.set(orders);
          this.status.set('ready');
        },
        error: () => {
          this.orders.set([]);
          this.status.set('error');
        },
      });
  }
}
