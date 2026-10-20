package io.github.nemm.study.inventory_reservation.common.error;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    HttpStatus getStatus();
    String getMessage();
    String getCode();
}
