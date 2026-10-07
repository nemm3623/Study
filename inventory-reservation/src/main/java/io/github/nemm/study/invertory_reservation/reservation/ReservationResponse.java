package io.github.nemm.study.invertory_reservation.reservation;

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
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(reservation.getId(), reservation.getProduct().getProductId(),
                reservation.getCustomerId(), reservation.getQuantity(), reservation.getStatus(),
                reservation.getCreatedAt(), reservation.getExpiresAt());
    }
}
