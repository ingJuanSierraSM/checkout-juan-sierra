export type DiscountType = 'CATEGORY' | 'VOLUME' | 'COUPON';

export interface CheckoutItemRequest {
  readonly productId: number;
  readonly quantity: number;
}

export interface CheckoutQuoteRequest {
  readonly items: readonly CheckoutItemRequest[];
  readonly couponCode: string | null;
}

export interface DiscountDetail {
  readonly type: DiscountType;
  readonly name: string;
  readonly percentage: number;
  readonly amount: number;
  readonly sequence: number;
}

export interface CheckoutQuote {
  readonly originalSubtotal: number;
  readonly discounts: readonly DiscountDetail[];
  readonly calculatedDiscountBeforeCap: number;
  readonly totalDiscount: number;
  readonly effectiveDiscountPercentage: number;
  readonly finalTotal: number;
  readonly discountCapApplied: boolean;
  readonly maximumDiscountPercentage: number;
}

export interface CheckoutCompleted extends CheckoutQuote {
  readonly orderId: number;
  readonly createdAt: string;
}

export interface CheckoutUiError {
  readonly code: string;
  readonly message: string;
}
