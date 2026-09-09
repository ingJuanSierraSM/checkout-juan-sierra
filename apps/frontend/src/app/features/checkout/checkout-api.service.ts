import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../core/api/api.config';
import { CheckoutCompleted, CheckoutQuote, CheckoutQuoteRequest } from './checkout.model';

@Injectable({ providedIn: 'root' })
export class CheckoutApiService {
  readonly #http = inject(HttpClient);
  readonly #apiBaseUrl = inject(API_BASE_URL);

  quote(request: CheckoutQuoteRequest): Observable<CheckoutQuote> {
    return this.#http.post<CheckoutQuote>(`${this.#apiBaseUrl}/checkout/quote`, request);
  }

  process(request: CheckoutQuoteRequest): Observable<CheckoutCompleted> {
    return this.#http.post<CheckoutCompleted>(`${this.#apiBaseUrl}/checkout`, request);
  }
}
