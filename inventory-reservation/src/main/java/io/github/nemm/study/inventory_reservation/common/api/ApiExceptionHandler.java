package io.github.nemm.study.inventory_reservation.common.api;

import io.github.nemm.study.inventory_reservation.common.error.BusinessException;
import io.github.nemm.study.inventory_reservation.common.error.CommonErrorCode;
import io.github.nemm.study.inventory_reservation.common.error.ErrorCode;
import io.github.nemm.study.inventory_reservation.product.error.ProductErrorCode;
import io.github.nemm.study.inventory_reservation.reservation.error.ReservationErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException exception) {
        return ResponseEntity.status(statusFor(exception.getCode()))
                .body(new ErrorResponse(exception.getCode().code(), exception.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> handleInvalidRequest(Exception exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(CommonErrorCode.INVALID_REQUEST.code(), "invalid request"));
    }

    private HttpStatus statusFor(ErrorCode code) {
        if (code == CommonErrorCode.INVALID_REQUEST) {
            return HttpStatus.BAD_REQUEST;
        }
        if (code == ProductErrorCode.PRODUCT_NOT_FOUND || code == ReservationErrorCode.RESERVATION_NOT_FOUND) {
            return HttpStatus.NOT_FOUND;
        }
        if (code == ProductErrorCode.INSUFFICIENT_STOCK
                || code == ReservationErrorCode.DUPLICATE_REQUEST_MISMATCH
                || code == ReservationErrorCode.INVALID_RESERVATION_STATE) {
            return HttpStatus.CONFLICT;
        }
        throw new IllegalArgumentException("Unmapped error code: " + code.code());
    }
}
