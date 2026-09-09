import { HttpErrorResponse } from '@angular/common/http';
import { ApiError } from '../../core/api/api-error.model';
import { CheckoutUiError } from './checkout.model';

function isApiError(value: unknown): value is ApiError {
  return (
    typeof value === 'object' &&
    value !== null &&
    'code' in value &&
    'message' in value &&
    typeof value.code === 'string' &&
    typeof value.message === 'string'
  );
}

export function toCheckoutUiError(
  httpError: unknown,
  fallbackCode: string,
  fallbackMessage: string,
): CheckoutUiError {
  if (httpError instanceof HttpErrorResponse && isApiError(httpError.error)) {
    return { code: httpError.error.code, message: httpError.error.message };
  }

  return { code: fallbackCode, message: fallbackMessage };
}
