package io.github.nemm.study.inventory_reservation.reservation.error;

import io.github.nemm.study.inventory_reservation.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReservationErrorCode implements ErrorCode {
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "reservation not found"),
    DUPLICATE_REQUEST_MISMATCH(HttpStatus.CONFLICT, "requestId was used with different reservation details"),
    INVALID_RESERVATION_STATE(HttpStatus.CONFLICT, "reservation cannot change to the requested state");

    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return name();
    }
}
