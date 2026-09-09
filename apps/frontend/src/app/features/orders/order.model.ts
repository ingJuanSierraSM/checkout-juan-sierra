import { ProductCategory } from '../catalog/product.model';
import { DiscountDetail, DiscountType } from '../checkout/checkout.model';

export interface OrderItem {
  readonly productId: number;
  readonly productName: string;
  readonly unitPrice: number;
  readonly category: ProductCategory;
  readonly quantity: number;
}

export interface OrderDiscount extends DiscountDetail {
  readonly type: DiscountType;
}

export interface Order {
  readonly id: number;
  readonly originalSubtotal: number;
  readonly calculatedDiscountBeforeCap: number;
  readonly totalDiscount: number;
  readonly effectiveDiscountPercentage: number;
  readonly finalTotal: number;
  readonly discountCapApplied: boolean;
  readonly createdAt: string;
  readonly items: readonly OrderItem[];
  readonly discounts: readonly OrderDiscount[];
}
