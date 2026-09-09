import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { API_BASE_URL } from '../../core/api/api.config';
import { OrderApiService } from './order-api.service';
import { Order } from './order.model';

describe('OrderApiService', () => {
  let service: OrderApiService;
  let httpTesting: HttpTestingController;
  const order: Order = {
    id: 1,
    originalSubtotal: 90,
    calculatedDiscountBeforeCap: 9,
    totalDiscount: 9,
    effectiveDiscountPercentage: 10,
    finalTotal: 81,
    discountCapApplied: false,
    createdAt: '2026-09-09T12:00:00Z',
    items: [{ productId: 2, productName: 'Smartphone X', unitPrice: 90, category: 'TECHNOLOGY', quantity: 1 }],
    discounts: [{ type: 'CATEGORY', name: 'Descuento Tecnología', percentage: 10, amount: 9, sequence: 1 }],
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        OrderApiService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: API_BASE_URL, useValue: 'http://api.test/api/v1' },
      ],
    });

    service = TestBed.inject(OrderApiService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('gets the order history', () => {
    service.list().subscribe((orders) => expect(orders).toEqual([order]));

    const request = httpTesting.expectOne('http://api.test/api/v1/orders');
    expect(request.request.method).toBe('GET');
    request.flush([order]);
  });

  it('gets an order by id', () => {
    service.getById(1).subscribe((response) => expect(response).toEqual(order));

    const request = httpTesting.expectOne('http://api.test/api/v1/orders/1');
    expect(request.request.method).toBe('GET');
    request.flush(order);
  });
});
