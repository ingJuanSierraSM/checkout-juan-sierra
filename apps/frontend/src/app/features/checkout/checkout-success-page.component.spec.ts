import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { vi } from 'vitest';
import { CheckoutProcessStore } from './checkout-process.store';
import { CheckoutCompleted } from './checkout.model';
import { CheckoutSuccessPageComponent } from './checkout-success-page.component';

describe('CheckoutSuccessPageComponent', () => {
  let fixture: ComponentFixture<CheckoutSuccessPageComponent>;
  const checkoutProcess = {
    completion: signal<CheckoutCompleted | null>(null),
    reset: vi.fn(),
  };
  const completion: CheckoutCompleted = {
    orderId: 42,
    createdAt: '2026-09-09T12:00:00Z',
    originalSubtotal: 120,
    discounts: [{ type: 'COUPON', name: 'Cupón WELCOME2026', percentage: 15, amount: 18, sequence: 1 }],
    calculatedDiscountBeforeCap: 18,
    totalDiscount: 18,
    effectiveDiscountPercentage: 15,
    finalTotal: 102,
    discountCapApplied: true,
    maximumDiscountPercentage: 35,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CheckoutSuccessPageComponent],
      providers: [
        provideRouter([]),
        { provide: CheckoutProcessStore, useValue: checkoutProcess },
      ],
    }).compileComponents();

    checkoutProcess.completion.set(null);
    checkoutProcess.reset.mockReset();
  });

  it('shows the completed order, its savings and the persistent cap notice', () => {
    checkoutProcess.completion.set(completion);
    fixture = TestBed.createComponent(CheckoutSuccessPageComponent);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('orden #42');
    expect(fixture.nativeElement.textContent).toContain('Cupón WELCOME2026');
    expect(fixture.nativeElement.textContent).toContain('límite máximo de ahorro permitido (35%)');
    expect(fixture.nativeElement.querySelector('.final-total strong')?.textContent).toContain('$102.00');
  });

  it('shows a safe fallback when the success route is opened without a confirmation', () => {
    fixture = TestBed.createComponent(CheckoutSuccessPageComponent);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('No encontramos una compra reciente');
  });

  it('clears the completion when the customer starts another purchase', () => {
    fixture = TestBed.createComponent(CheckoutSuccessPageComponent);
    fixture.componentInstance.startAnotherPurchase();

    expect(checkoutProcess.reset).toHaveBeenCalled();
  });
});
