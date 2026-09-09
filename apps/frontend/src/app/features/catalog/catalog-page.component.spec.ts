import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Observable, of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { CartStore } from '../cart/cart.store';
import { CheckoutQuoteStore } from '../checkout/checkout-quote.store';
import { CheckoutQuote, CheckoutUiError } from '../checkout/checkout.model';
import { CatalogPageComponent } from './catalog-page.component';
import { ProductApiService } from './product-api.service';
import { Product } from './product.model';

describe('CatalogPageComponent', () => {
  let fixture: ComponentFixture<CatalogPageComponent>;
  let getProducts: ReturnType<typeof vi.fn<() => Observable<readonly Product[]>>>;
  let cart: CartStore;
  const checkoutStore = {
    quote: signal<CheckoutQuote | null>(null),
    status: signal<'idle' | 'loading' | 'ready' | 'error'>('idle'),
    error: signal<CheckoutUiError | null>(null),
    appliedCoupon: signal<string | null>(null),
    refreshQuote: vi.fn(),
    applyCoupon: vi.fn(),
    removeCoupon: vi.fn(),
  };

  const products: readonly Product[] = [
    {
      id: 1,
      name: 'Laptop Pro',
      unitPrice: 120,
      category: 'TECHNOLOGY',
      stock: 5,
      imageUrl: '/products/laptop-pro.webp',
    },
    {
      id: 4,
      name: 'Office Chair',
      unitPrice: 80,
      category: 'HOME',
      stock: 8,
      imageUrl: '/products/office-chair.webp',
    },
    {
      id: 2,
      name: 'Smartphone X',
      unitPrice: 90,
      category: 'TECHNOLOGY',
      stock: 6,
      imageUrl: '/products/smartphone-x.webp',
    },
    {
      id: 3,
      name: 'Wireless Headphones',
      unitPrice: 40,
      category: 'TECHNOLOGY',
      stock: 10,
      imageUrl: '/products/wireless-headphones.webp',
    },
    {
      id: 5,
      name: 'Coffee Maker',
      unitPrice: 60,
      category: 'HOME',
      stock: 7,
      imageUrl: '/products/coffee-maker.webp',
    },
    {
      id: 6,
      name: 'Urban Backpack',
      unitPrice: 35,
      category: 'ACCESSORIES',
      stock: 12,
      imageUrl: '/products/urban-backpack.webp',
    },
  ];

  beforeEach(async () => {
    getProducts = vi.fn<() => Observable<readonly Product[]>>();

    await TestBed.configureTestingModule({
      imports: [CatalogPageComponent],
      providers: [
        provideRouter([]),
        { provide: ProductApiService, useValue: { getProducts } },
        { provide: CheckoutQuoteStore, useValue: checkoutStore },
      ],
    }).compileComponents();

    cart = TestBed.inject(CartStore);
    cart.clear();
    checkoutStore.quote.set(null);
    checkoutStore.status.set('idle');
    checkoutStore.error.set(null);
    checkoutStore.appliedCoupon.set(null);
    checkoutStore.refreshQuote.mockReset();
    checkoutStore.applyCoupon.mockReset();
    checkoutStore.removeCoupon.mockReset();
  });

  it('renders the products returned by the API', () => {
    getProducts.mockReturnValue(of(products));
    fixture = TestBed.createComponent(CatalogPageComponent);
    fixture.detectChanges();

    const cards = fixture.nativeElement.querySelectorAll('app-product-card');

    expect(cards).toHaveLength(6);
    expect(fixture.nativeElement.textContent).toContain('6 productos');
    expect(fixture.nativeElement.textContent).toContain('Laptop Pro');
  });

  it('shows an error state and allows retrying the request', () => {
    getProducts
      .mockReturnValueOnce(throwError(() => new Error('API unavailable')))
      .mockReturnValueOnce(of([]));
    fixture = TestBed.createComponent(CatalogPageComponent);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('[role="alert"]')?.textContent).toContain(
      'No pudimos cargar el catálogo',
    );

    const retryButton = fixture.nativeElement.querySelector('button') as HTMLButtonElement;
    retryButton.click();
    fixture.detectChanges();

    expect(getProducts).toHaveBeenCalledTimes(2);
    expect(fixture.nativeElement.textContent).toContain('Aún no hay productos disponibles');
  });

  it('adds products from the catalog and updates the cart controls and subtotal', () => {
    getProducts.mockReturnValue(of(products));
    fixture = TestBed.createComponent(CatalogPageComponent);
    fixture.detectChanges();

    const firstProductCard = fixture.nativeElement.querySelector('app-product-card') as HTMLElement;
    const addButton = firstProductCard.querySelector('button') as HTMLButtonElement;
    addButton.click();
    fixture.detectChanges();

    expect(cart.itemCount()).toBe(1);
    expect(fixture.nativeElement.querySelector('.mobile-cart-bar strong')?.textContent).toContain('$120.00');

    const quantityButtons = fixture.nativeElement.querySelectorAll('.cart-item .quantity-control button');
    (quantityButtons[1] as HTMLButtonElement).click();
    fixture.detectChanges();
    expect(cart.quantityFor(products[0].id)).toBe(2);

    (quantityButtons[0] as HTMLButtonElement).click();
    fixture.detectChanges();
    expect(cart.quantityFor(products[0].id)).toBe(1);

    const removeButton = fixture.nativeElement.querySelector('.remove-item') as HTMLButtonElement;
    removeButton.click();
    fixture.detectChanges();
    expect(cart.isEmpty()).toBe(true);
  });

  it('opens and closes the mobile cart for both empty and populated carts', () => {
    getProducts.mockReturnValue(of(products));
    fixture = TestBed.createComponent(CatalogPageComponent);
    fixture.detectChanges();

    fixture.componentInstance.openMobileCart();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Agrega productos desde el catálogo');

    fixture.componentInstance.closeMobileCart();
    cart.add(products[0]);
    fixture.componentInstance.openMobileCart();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelectorAll('.mobile-cart-items li')).toHaveLength(1);
    expect(fixture.nativeElement.querySelector('.mobile-sheet-total strong')?.textContent).toContain('$120.00');
  });

  it('forwards the coupon entered by the customer and lets them remove it', () => {
    getProducts.mockReturnValue(of(products));
    fixture = TestBed.createComponent(CatalogPageComponent);
    fixture.detectChanges();

    const firstProductCard = fixture.nativeElement.querySelector('app-product-card') as HTMLElement;
    (firstProductCard.querySelector('button') as HTMLButtonElement).click();
    fixture.detectChanges();

    const couponInput = fixture.nativeElement.querySelector('#coupon-code') as HTMLInputElement;
    couponInput.value = ' welcome2026 ';
    couponInput.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    const applyButton = fixture.nativeElement.querySelector('.coupon-input-row button') as HTMLButtonElement;
    applyButton.click();
    expect(checkoutStore.applyCoupon).toHaveBeenCalledWith(' welcome2026 ');

    checkoutStore.appliedCoupon.set('WELCOME2026');
    fixture.detectChanges();
    (fixture.nativeElement.querySelector('.applied-coupon button') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(checkoutStore.removeCoupon).toHaveBeenCalled();
    expect((fixture.nativeElement.querySelector('#coupon-code') as HTMLInputElement).value).toBe('');
  });
});
