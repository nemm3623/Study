package io.github.nemm.study.inventory_reservation.common.api;

import io.github.nemm.study.inventory_reservation.common.error.ErrorCode;

public record ErrorResponse(String code, String message) {

    public ErrorResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getMessage());
    }
}
