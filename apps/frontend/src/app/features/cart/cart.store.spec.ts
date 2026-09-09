import { Product } from '../catalog/product.model';
import { CartStore } from './cart.store';

describe('CartStore', () => {
  const laptop: Product = {
    id: 1,
    name: 'Laptop Pro',
    unitPrice: 120,
    category: 'TECHNOLOGY',
    stock: 2,
    imageUrl: '/products/laptop-pro.webp',
  };

  const coffeeMaker: Product = {
    id: 5,
    name: 'Coffee Maker',
    unitPrice: 60,
    category: 'HOME',
    stock: 4,
    imageUrl: '/products/coffee-maker.webp',
  };

  let store: CartStore;

  beforeEach(() => {
    store = new CartStore();
  });

  it('adds products and calculates original subtotal and item count', () => {
    store.add(laptop);
    store.add(coffeeMaker);
    store.add(coffeeMaker);

    expect(store.itemCount()).toBe(3);
    expect(store.originalSubtotal()).toBe(240);
    expect(store.quantityFor(coffeeMaker.id)).toBe(2);
  });

  it('does not allow a quantity greater than the available stock', () => {
    expect(store.add(laptop)).toBe(true);
    expect(store.add(laptop)).toBe(true);
    expect(store.add(laptop)).toBe(false);

    expect(store.quantityFor(laptop.id)).toBe(2);
    expect(store.itemCount()).toBe(2);
  });

  it('removes an item when decreasing its quantity from one', () => {
    store.add(laptop);

    store.decrease(laptop.id);

    expect(store.isEmpty()).toBe(true);
    expect(store.originalSubtotal()).toBe(0);
  });

  it('decreases an existing item without removing it and ignores unknown products', () => {
    store.add(laptop);
    store.add(laptop);

    store.decrease(laptop.id);
    store.decrease(999);

    expect(store.quantityFor(laptop.id)).toBe(1);
    expect(store.itemCount()).toBe(1);
  });

  it('removes a specific item and can clear the complete cart', () => {
    store.add(laptop);
    store.add(coffeeMaker);

    store.remove(laptop.id);
    expect(store.items()).toEqual([{ product: coffeeMaker, quantity: 1 }]);

    store.clear();
    expect(store.items()).toEqual([]);
  });
});
