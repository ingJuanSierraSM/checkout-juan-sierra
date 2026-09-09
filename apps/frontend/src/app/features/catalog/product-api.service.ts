import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../core/api/api.config';
import { Product } from './product.model';

@Injectable({ providedIn: 'root' })
export class ProductApiService {
  readonly #http = inject(HttpClient);
  readonly #apiBaseUrl = inject(API_BASE_URL);

  getProducts(): Observable<readonly Product[]> {
    return this.#http.get<readonly Product[]>(`${this.#apiBaseUrl}/products`);
  }
}
