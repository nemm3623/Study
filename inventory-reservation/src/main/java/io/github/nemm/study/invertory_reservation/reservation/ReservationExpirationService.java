package io.github.nemm.study.invertory_reservation.reservation;

import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationExpirationService {

    private final ReservationRepository reservations;
    private final Clock clock;

    public ReservationExpirationService(ReservationRepository reservations, Clock clock) {
        this.reservations = reservations;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void expireDue() {
        Instant now = clock.instant();
        for (Reservation reservation : reservations.findByStatusAndExpiresAtLessThanEqual(
                ReservationStatus.RESERVED, now)) {
            reservation.expire(now);
        }
    }
}
