package io.github.nemm.study.inventory_reservation.common.api;

import io.github.nemm.study.inventory_reservation.common.error.BusinessException;
import io.github.nemm.study.inventory_reservation.common.error.CommonErrorCode;
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
        return ResponseEntity.status(exception.getCode().getStatus())
                .body(new ErrorResponse(exception.getCode()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> handleInvalidRequest(Exception exception) {
        return ResponseEntity.status(CommonErrorCode.INVALID_REQUEST.getStatus())
                .body(new ErrorResponse(CommonErrorCode.INVALID_REQUEST));
    }
}
