package io.github.nemm.study.inventory_reservation.common.error;

public enum CommonErrorCode implements ErrorCode {
    INVALID_REQUEST;

    @Override
    public String code() {
        return name();
    }
}
