import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../core/api/api.config';
import { Order } from './order.model';

@Injectable({ providedIn: 'root' })
export class OrderApiService {
  readonly #http = inject(HttpClient);
  readonly #apiBaseUrl = inject(API_BASE_URL);

  list(): Observable<readonly Order[]> {
    return this.#http.get<readonly Order[]>(`${this.#apiBaseUrl}/orders`);
  }

  getById(orderId: number): Observable<Order> {
    return this.#http.get<Order>(`${this.#apiBaseUrl}/orders/${orderId}`);
  }
}
