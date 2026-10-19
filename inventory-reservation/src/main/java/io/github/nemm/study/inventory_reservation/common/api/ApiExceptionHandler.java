package io.github.nemm.study.inventory_reservation.common.api;

import io.github.nemm.study.inventory_reservation.common.application.ApplicationException;
import io.github.nemm.study.inventory_reservation.common.application.ErrorCode;
import io.github.nemm.study.inventory_reservation.product.domain.InsufficientStockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(ApplicationException exception) {
        HttpStatus status = switch (exception.getCode()) {
            case INVALID_REQUEST -> HttpStatus.BAD_REQUEST;
            case PRODUCT_NOT_FOUND, RESERVATION_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case DUPLICATE_REQUEST_MISMATCH, INVALID_RESERVATION_STATE -> HttpStatus.CONFLICT;
        };
        return ResponseEntity.status(status)
                .body(new ErrorResponse(exception.getCode().name(), exception.getMessage()));
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(InsufficientStockException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("INSUFFICIENT_STOCK", exception.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> handleInvalidRequest(Exception exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ErrorCode.INVALID_REQUEST.name(), "invalid request"));
    }
}
