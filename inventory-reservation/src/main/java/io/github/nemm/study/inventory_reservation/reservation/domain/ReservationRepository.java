package io.github.nemm.study.inventory_reservation.reservation.domain;

import io.github.nemm.study.inventory_reservation.product.domain.Product;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {
    Optional<Reservation> findById(String reservationId);

    Optional<Reservation> findByProductAndRequestId(Product product, String requestId);

    List<Reservation> findByStatusAndExpiresAtLessThanEqual(ReservationStatus status, Instant now);

    Reservation save(Reservation reservation);
}
