package com.ecommerce.core.shared.infrastructure.web;

import com.ecommerce.core.shared.application.exception.BusinessErrorCode;
import com.ecommerce.core.shared.application.exception.BusinessException;
import com.ecommerce.core.shared.infrastructure.web.dto.ApiErrorResponse;
import com.ecommerce.core.shared.infrastructure.web.dto.ValidationError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(
            BusinessException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = statusFor(exception.errorCode());
        return ResponseEntity.status(status).body(error(
                status,
                exception.errorCode().name(),
                exception.getMessage(),
                request.getRequestURI(),
                List.of()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<ValidationError> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new ValidationError(error.getField(), error.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest().body(error(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "La solicitud contiene campos inválidos",
                request.getRequestURI(),
                errors
        ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        List<ValidationError> errors = exception.getConstraintViolations().stream()
                .map(error -> new ValidationError(error.getPropertyPath().toString(), error.getMessage()))
                .toList();
        return ResponseEntity.badRequest().body(error(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "La solicitud contiene campos inválidos",
                request.getRequestURI(),
                errors
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "Ocurrió un error interno inesperado",
                request.getRequestURI(),
                List.of()
        ));
    }

    private HttpStatus statusFor(BusinessErrorCode errorCode) {
        return switch (errorCode) {
            case EMPTY_CART, INVALID_CART -> HttpStatus.BAD_REQUEST;
            case PRODUCT_NOT_FOUND, COUPON_NOT_FOUND, ORDER_NOT_FOUND, DISCOUNT_POLICY_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case INSUFFICIENT_STOCK, COUPON_ALREADY_USED -> HttpStatus.CONFLICT;
            case INACTIVE_PRODUCT, COUPON_INACTIVE, COUPON_EXPIRED -> HttpStatus.UNPROCESSABLE_CONTENT;
        };
    }

    private ApiErrorResponse error(
            HttpStatus status,
            String code,
            String message,
            String path,
            List<ValidationError> validationErrors
    ) {
        return new ApiErrorResponse(Instant.now(), status.value(), code, message, path, validationErrors);
    }
}
