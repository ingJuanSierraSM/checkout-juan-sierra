import { Routes } from '@angular/router';
import { CatalogPageComponent } from './features/catalog/catalog-page.component';
import { CheckoutSuccessPageComponent } from './features/checkout/checkout-success-page.component';

export const routes: Routes = [
  {
    path: '',
    component: CatalogPageComponent,
    title: 'DaviShop | Catálogo',
  },
  {
    path: 'checkout/success',
    component: CheckoutSuccessPageComponent,
    title: 'DaviShop | Compra confirmada',
  },
  {
    path: '**',
    redirectTo: '',
  },
];
