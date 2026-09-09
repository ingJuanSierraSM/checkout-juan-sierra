import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { API_BASE_URL } from '../../core/api/api.config';
import { ProductApiService } from './product-api.service';
import { Product } from './product.model';

describe('ProductApiService', () => {
  let service: ProductApiService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ProductApiService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: API_BASE_URL, useValue: 'http://api.test/api/v1' },
      ],
    });

    service = TestBed.inject(ProductApiService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('gets the active product catalog from the versioned API', () => {
    const products: readonly Product[] = [
      {
        id: 1,
        name: 'Laptop Pro',
        unitPrice: 120,
        category: 'TECHNOLOGY',
        stock: 5,
        imageUrl: '/products/laptop-pro.webp',
      },
    ];

    service.getProducts().subscribe((response) => {
      expect(response).toEqual(products);
    });

    const request = httpTesting.expectOne('http://api.test/api/v1/products');
    expect(request.request.method).toBe('GET');
    request.flush(products);
  });
});
