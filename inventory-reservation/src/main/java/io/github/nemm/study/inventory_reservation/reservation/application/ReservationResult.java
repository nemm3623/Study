package io.github.nemm.study.inventory_reservation.reservation.application;

import io.github.nemm.study.inventory_reservation.reservation.domain.Reservation;
import io.github.nemm.study.inventory_reservation.reservation.domain.ReservationStatus;
import java.time.Instant;

public record ReservationResult(
        String reservationId,
        String productId,
        String customerId,
        int quantity,
        ReservationStatus status,
        Instant createdAt,
        Instant expiresAt
) {
    public static ReservationResult from(Reservation reservation) {
        return new ReservationResult(reservation.getId(), reservation.getProduct().getProductId(),
                reservation.getCustomerId(), reservation.getQuantity(), reservation.getStatus(),
                reservation.getCreatedAt(), reservation.getExpiresAt());
    }
}
