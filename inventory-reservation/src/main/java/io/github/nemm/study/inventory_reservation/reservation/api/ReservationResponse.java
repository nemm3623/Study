package io.github.nemm.study.inventory_reservation.reservation.api;

import io.github.nemm.study.inventory_reservation.reservation.application.ReservationResult;
import io.github.nemm.study.inventory_reservation.reservation.domain.ReservationStatus;
import java.time.Instant;

public record ReservationResponse(
        String reservationId,
        String productId,
        String customerId,
        int quantity,
        ReservationStatus status,
        Instant createdAt,
        Instant expiresAt
) {
    public static ReservationResponse from(ReservationResult result) {
        return new ReservationResponse(result.reservationId(), result.productId(),
                result.customerId(), result.quantity(), result.status(),
                result.createdAt(), result.expiresAt());
    }
}
