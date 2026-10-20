package io.github.nemm.study.inventory_reservation.reservation.error;

import io.github.nemm.study.inventory_reservation.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReservationErrorCode implements ErrorCode {
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "예약을 찾을 수 없습니다."),
    DUPLICATE_REQUEST_MISMATCH(HttpStatus.CONFLICT, "같은 requestId로 다른 예약 내용을 요청했습니다."),
    INVALID_RESERVATION_STATE(HttpStatus.CONFLICT, "현재 예약 상태에서는 요청한 상태로 변경할 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return name();
    }
}
