import { Routes } from '@angular/router';
import { CatalogPageComponent } from './features/catalog/catalog-page.component';
import { CheckoutSuccessPageComponent } from './features/checkout/checkout-success-page.component';
import { OrderDetailPageComponent } from './features/orders/order-detail-page.component';
import { OrdersPageComponent } from './features/orders/orders-page.component';

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
    path: 'orders',
    component: OrdersPageComponent,
    title: 'DaviShop | Mis órdenes',
  },
  {
    path: 'orders/:orderId',
    component: OrderDetailPageComponent,
    title: 'DaviShop | Detalle de orden',
  },
  {
    path: '**',
    redirectTo: '',
  },
];
