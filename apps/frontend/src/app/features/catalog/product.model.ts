export type ProductCategory = 'TECHNOLOGY' | 'HOME' | 'ACCESSORIES';

export interface Product {
  readonly id: number;
  readonly name: string;
  readonly unitPrice: number;
  readonly category: ProductCategory;
  readonly stock: number;
  readonly imageUrl: string;
}

const categoryLabels: Record<ProductCategory, string> = {
  TECHNOLOGY: 'Tecnología',
  HOME: 'Hogar',
  ACCESSORIES: 'Accesorios',
};

export function getCategoryLabel(category: ProductCategory): string {
  return categoryLabels[category];
}
