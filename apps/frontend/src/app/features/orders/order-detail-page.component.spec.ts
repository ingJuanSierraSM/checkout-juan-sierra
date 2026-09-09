import { ComponentFixture, TestBed } from '@angular/core/testing';
import { convertToParamMap } from '@angular/router';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { Observable, of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { OrderApiService } from './order-api.service';
import { Order } from './order.model';
import { OrderDetailPageComponent } from './order-detail-page.component';

describe('OrderDetailPageComponent', () => {
  let fixture: ComponentFixture<OrderDetailPageComponent>;
  let getById: ReturnType<typeof vi.fn<(orderId: number) => Observable<Order>>>;
  const order: Order = {
    id: 1,
    originalSubtotal: 90,
    calculatedDiscountBeforeCap: 9,
    totalDiscount: 9,
    effectiveDiscountPercentage: 10,
    finalTotal: 81,
    discountCapApplied: true,
    createdAt: '2026-09-09T12:00:00Z',
    items: [{ productId: 2, productName: 'Smartphone X', unitPrice: 90, category: 'TECHNOLOGY', quantity: 1 }],
    discounts: [{ type: 'CATEGORY', name: 'Descuento Tecnología', percentage: 10, amount: 9, sequence: 1 }],
  };

  beforeEach(async () => {
    getById = vi.fn<(orderId: number) => Observable<Order>>().mockReturnValue(of(order));

    await TestBed.configureTestingModule({
      imports: [OrderDetailPageComponent],
      providers: [
        provideRouter([]),
        { provide: OrderApiService, useValue: { getById } },
        { provide: ActivatedRoute, useValue: { paramMap: of(convertToParamMap({ orderId: '1' })) } },
      ],
    }).compileComponents();
  });

  it('shows the products, totals and persistent cap notice for an order', () => {
    fixture = TestBed.createComponent(OrderDetailPageComponent);
    fixture.detectChanges();

    expect(getById).toHaveBeenCalledWith(1);
    expect(fixture.nativeElement.textContent).toContain('Smartphone X');
    expect(fixture.nativeElement.textContent).toContain('límite máximo de ahorro permitido (35%)');
    expect(fixture.nativeElement.querySelector('.final-total strong')?.textContent).toContain('$81.00');
  });

  it('shows an error and retries a failed detail request', () => {
    getById
      .mockReturnValueOnce(throwError(() => new Error('Unavailable')))
      .mockReturnValueOnce(of(order));
    fixture = TestBed.createComponent(OrderDetailPageComponent);
    fixture.detectChanges();

    (fixture.nativeElement.querySelector('.detail-feedback button') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(getById).toHaveBeenCalledTimes(2);
    expect(fixture.nativeElement.textContent).toContain('Orden #1');
  });
});
