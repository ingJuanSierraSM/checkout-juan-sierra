import { CurrencyPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { getCategoryLabel, Product } from './product.model';

@Component({
  selector: 'app-product-card',
  imports: [CurrencyPipe],
  templateUrl: './product-card.component.html',
  styleUrl: './product-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProductCardComponent {
  readonly product = input.required<Product>();
  readonly quantityInCart = input(0);
  readonly isCheckoutProcessing = input(false);
  readonly addToCart = output<Product>();
  readonly categoryLabel = computed(() => getCategoryLabel(this.product().category));
  readonly remainingStock = computed(() => this.product().stock - this.quantityInCart());
  readonly canAdd = computed(() => this.remainingStock() > 0);
}
