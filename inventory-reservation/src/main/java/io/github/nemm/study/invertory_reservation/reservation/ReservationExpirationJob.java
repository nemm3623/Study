package io.github.nemm.study.invertory_reservation.reservation;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservationExpirationJob {

    private final ReservationExpirationService expirationService;

    public ReservationExpirationJob(ReservationExpirationService expirationService) {
        this.expirationService = expirationService;
    }

    @Scheduled(fixedDelay = 1000)
    public void expireDue() {
        expirationService.expireDue();
    }
}
