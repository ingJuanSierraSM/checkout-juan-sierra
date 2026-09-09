export interface ApiValidationError {
  readonly field: string;
  readonly message: string;
}

export interface ApiError {
  readonly timestamp: string;
  readonly status: number;
  readonly code: string;
  readonly message: string;
  readonly path: string;
  readonly validationErrors: readonly ApiValidationError[];
}
