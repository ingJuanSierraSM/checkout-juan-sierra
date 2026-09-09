import { Routes } from '@angular/router';
import { CatalogPageComponent } from './features/catalog/catalog-page.component';

export const routes: Routes = [
  {
    path: '',
    component: CatalogPageComponent,
    title: 'DaviShop | Catálogo',
  },
  {
    path: '**',
    redirectTo: '',
  },
];
