package io.github.nemm.study.inventory_reservation.reservation.error;

import io.github.nemm.study.inventory_reservation.common.error.ErrorCode;

public enum ReservationErrorCode implements ErrorCode {
    RESERVATION_NOT_FOUND,
    DUPLICATE_REQUEST_MISMATCH,
    INVALID_RESERVATION_STATE;

    @Override
    public String code() {
        return name();
    }
}
