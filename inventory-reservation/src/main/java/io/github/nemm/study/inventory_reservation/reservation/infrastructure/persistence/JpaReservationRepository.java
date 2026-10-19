package io.github.nemm.study.inventory_reservation.reservation.infrastructure.persistence;

import io.github.nemm.study.inventory_reservation.product.domain.Product;
import io.github.nemm.study.inventory_reservation.reservation.domain.Reservation;
import io.github.nemm.study.inventory_reservation.reservation.domain.ReservationStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaReservationRepository extends JpaRepository<Reservation, String> {
    Optional<Reservation> findByProductAndRequestId(Product product, String requestId);

    List<Reservation> findByStatusAndExpiresAtLessThanEqual(ReservationStatus status, Instant now);
}
