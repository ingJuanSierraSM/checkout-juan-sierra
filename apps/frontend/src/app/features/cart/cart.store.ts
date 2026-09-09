import { Injectable, computed, signal } from '@angular/core';
import { Product } from '../catalog/product.model';

export interface CartItem {
  readonly product: Product;
  readonly quantity: number;
}

@Injectable({ providedIn: 'root' })
export class CartStore {
  readonly #items = signal<readonly CartItem[]>([]);

  readonly items = this.#items.asReadonly();
  readonly itemCount = computed(() =>
    this.#items().reduce((total, item) => total + item.quantity, 0),
  );
  readonly originalSubtotal = computed(() =>
    this.#items().reduce((total, item) => total + item.product.unitPrice * item.quantity, 0),
  );
  readonly isEmpty = computed(() => this.#items().length === 0);

  add(product: Product): boolean {
    const currentQuantity = this.quantityFor(product.id);

    if (currentQuantity >= product.stock) {
      return false;
    }

    this.#items.update((items) => {
      const existingItem = items.find((item) => item.product.id === product.id);

      if (!existingItem) {
        return [...items, { product, quantity: 1 }];
      }

      return items.map((item) =>
        item.product.id === product.id ? { ...item, quantity: item.quantity + 1 } : item,
      );
    });

    return true;
  }

  decrease(productId: number): void {
    this.#items.update((items) => {
      const item = items.find((currentItem) => currentItem.product.id === productId);

      if (!item) {
        return items;
      }

      if (item.quantity === 1) {
        return items.filter((currentItem) => currentItem.product.id !== productId);
      }

      return items.map((currentItem) =>
        currentItem.product.id === productId
          ? { ...currentItem, quantity: currentItem.quantity - 1 }
          : currentItem,
      );
    });
  }

  remove(productId: number): void {
    this.#items.update((items) => items.filter((item) => item.product.id !== productId));
  }

  clear(): void {
    this.#items.set([]);
  }

  quantityFor(productId: number): number {
    return this.#items().find((item) => item.product.id === productId)?.quantity ?? 0;
  }
}
