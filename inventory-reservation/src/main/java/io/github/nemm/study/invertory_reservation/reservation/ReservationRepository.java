package io.github.nemm.study.invertory_reservation.reservation;

import io.github.nemm.study.invertory_reservation.product.Product;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, String> {
    Optional<Reservation> findByProductAndRequestId(Product product, String requestId);
}
