package io.github.nemm.study.inventory_reservation.reservation.api;

import io.github.nemm.study.inventory_reservation.reservation.application.CreateReservationCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateReservationRequest(
        @NotBlank String customerId,
        @NotNull @Positive Integer quantity,
        @NotBlank String requestId
) {
    public CreateReservationCommand toCommand() {
        return new CreateReservationCommand(customerId, quantity, requestId);
    }
}
