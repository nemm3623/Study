package io.github.nemm.study.inventory_reservation.product.error;

import io.github.nemm.study.inventory_reservation.common.error.ErrorCode;

public enum ProductErrorCode implements ErrorCode {
    PRODUCT_NOT_FOUND,
    INSUFFICIENT_STOCK;

    @Override
    public String code() {
        return name();
    }
}
