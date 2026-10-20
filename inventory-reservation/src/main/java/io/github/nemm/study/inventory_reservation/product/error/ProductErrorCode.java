package io.github.nemm.study.inventory_reservation.product.error;

import io.github.nemm.study.inventory_reservation.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "product not found"),
    INSUFFICIENT_STOCK(HttpStatus.CONFLICT, "not enough available stock");

    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return name();
    }
}
