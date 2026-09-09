import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { API_BASE_URL } from '../../core/api/api.config';
import { CheckoutApiService } from './checkout-api.service';
import { CheckoutQuote, CheckoutQuoteRequest } from './checkout.model';

describe('CheckoutApiService', () => {
  let service: CheckoutApiService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        CheckoutApiService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: API_BASE_URL, useValue: 'http://api.test/api/v1' },
      ],
    });

    service = TestBed.inject(CheckoutApiService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('sends the cart and coupon to the quote endpoint', () => {
    const request: CheckoutQuoteRequest = {
      items: [{ productId: 1, quantity: 1 }],
      couponCode: 'WELCOME2026',
    };
    const quote: CheckoutQuote = {
      originalSubtotal: 120,
      discounts: [],
      calculatedDiscountBeforeCap: 0,
      totalDiscount: 0,
      effectiveDiscountPercentage: 0,
      finalTotal: 120,
      discountCapApplied: false,
      maximumDiscountPercentage: 35,
    };

    service.quote(request).subscribe((response) => expect(response).toEqual(quote));

    const httpRequest = httpTesting.expectOne('http://api.test/api/v1/checkout/quote');
    expect(httpRequest.request.method).toBe('POST');
    expect(httpRequest.request.body).toEqual(request);
    httpRequest.flush(quote);
  });
});
