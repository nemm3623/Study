package io.github.nemm.study.inventory_reservation.common.application;

public class ApplicationException extends RuntimeException {

    private final ErrorCode code;

    public ApplicationException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ErrorCode getCode() {
        return code;
    }
}
