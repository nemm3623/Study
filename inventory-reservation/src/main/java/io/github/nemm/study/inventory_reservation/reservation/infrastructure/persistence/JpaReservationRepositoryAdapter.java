package io.github.nemm.study.inventory_reservation.reservation.infrastructure.persistence;

import io.github.nemm.study.inventory_reservation.product.domain.Product;
import io.github.nemm.study.inventory_reservation.reservation.domain.Reservation;
import io.github.nemm.study.inventory_reservation.reservation.domain.ReservationRepository;
import io.github.nemm.study.inventory_reservation.reservation.domain.ReservationStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaReservationRepositoryAdapter implements ReservationRepository {

    private final JpaReservationRepository jpaRepository;

    public JpaReservationRepositoryAdapter(JpaReservationRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Reservation> findById(String reservationId) {
        return jpaRepository.findById(reservationId);
    }

    @Override
    public Optional<Reservation> findByProductAndRequestId(Product product, String requestId) {
        return jpaRepository.findByProductAndRequestId(product, requestId);
    }

    @Override
    public List<Reservation> findByStatusAndExpiresAtLessThanEqual(ReservationStatus status, Instant now) {
        return jpaRepository.findByStatusAndExpiresAtLessThanEqual(status, now);
    }

    @Override
    public Reservation save(Reservation reservation) {
        return jpaRepository.save(reservation);
    }
}
