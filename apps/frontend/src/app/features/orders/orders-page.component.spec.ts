import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Observable, of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { OrderApiService } from './order-api.service';
import { Order } from './order.model';
import { OrdersPageComponent } from './orders-page.component';

describe('OrdersPageComponent', () => {
  let fixture: ComponentFixture<OrdersPageComponent>;
  let listOrders: ReturnType<typeof vi.fn<() => Observable<readonly Order[]>>>;
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

  beforeEach(async () => {
    listOrders = vi.fn<() => Observable<readonly Order[]>>();

    await TestBed.configureTestingModule({
      imports: [OrdersPageComponent],
      providers: [
        provideRouter([]),
        { provide: OrderApiService, useValue: { list: listOrders } },
      ],
    }).compileComponents();
  });

  it('shows the orders returned by the API with their detail link', () => {
    listOrders.mockReturnValue(of([order]));
    fixture = TestBed.createComponent(OrdersPageComponent);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Orden #1');
    expect(fixture.nativeElement.textContent).toContain('$81.00');
    expect(fixture.nativeElement.querySelector('.order-row')?.getAttribute('href')).toBe('/orders/1');
  });

  it('shows the empty state when there are no orders', () => {
    listOrders.mockReturnValue(of([]));
    fixture = TestBed.createComponent(OrdersPageComponent);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Aún no tienes órdenes');
  });

  it('shows an error state and retries the request', () => {
    listOrders
      .mockReturnValueOnce(throwError(() => new Error('Unavailable')))
      .mockReturnValueOnce(of([]));
    fixture = TestBed.createComponent(OrdersPageComponent);
    fixture.detectChanges();

    (fixture.nativeElement.querySelector('.orders-feedback button') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(listOrders).toHaveBeenCalledTimes(2);
    expect(fixture.nativeElement.textContent).toContain('Aún no tienes órdenes');
  });
});
