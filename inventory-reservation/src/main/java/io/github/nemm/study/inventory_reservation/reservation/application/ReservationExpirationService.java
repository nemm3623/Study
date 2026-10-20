package io.github.nemm.study.inventory_reservation.reservation.application;

import io.github.nemm.study.inventory_reservation.reservation.domain.Reservation;
import io.github.nemm.study.inventory_reservation.reservation.domain.ReservationRepository;
import io.github.nemm.study.inventory_reservation.reservation.domain.ReservationStatus;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationExpirationService {

    private final ReservationRepository reservations;
    private final Clock clock;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void expireDue() {
        Instant now = clock.instant();
        for (Reservation reservation : reservations.findByStatusAndExpiresAtLessThanEqual(
                ReservationStatus.RESERVED, now)) {
            reservation.expire(now);
        }
    }
}
