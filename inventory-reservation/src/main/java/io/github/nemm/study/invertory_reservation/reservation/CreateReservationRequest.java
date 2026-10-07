package io.github.nemm.study.invertory_reservation.reservation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateReservationRequest(
        @NotBlank String customerId,
        @NotNull @Positive Integer quantity,
        @NotBlank String requestId
) {
}
