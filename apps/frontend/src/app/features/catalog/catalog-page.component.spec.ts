import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Observable, of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { CatalogPageComponent } from './catalog-page.component';
import { ProductApiService } from './product-api.service';
import { Product } from './product.model';

describe('CatalogPageComponent', () => {
  let fixture: ComponentFixture<CatalogPageComponent>;
  let getProducts: ReturnType<typeof vi.fn<() => Observable<readonly Product[]>>>;

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
  ];

  beforeEach(async () => {
    getProducts = vi.fn<() => Observable<readonly Product[]>>();

    await TestBed.configureTestingModule({
      imports: [CatalogPageComponent],
      providers: [
        provideRouter([]),
        { provide: ProductApiService, useValue: { getProducts } },
      ],
    }).compileComponents();
  });

  it('renders the products returned by the API', () => {
    getProducts.mockReturnValue(of(products));
    fixture = TestBed.createComponent(CatalogPageComponent);
    fixture.detectChanges();

    const cards = fixture.nativeElement.querySelectorAll('app-product-card');

    expect(cards).toHaveLength(2);
    expect(fixture.nativeElement.textContent).toContain('2 productos');
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
});
