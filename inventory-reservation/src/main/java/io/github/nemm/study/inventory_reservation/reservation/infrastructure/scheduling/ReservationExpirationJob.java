package io.github.nemm.study.inventory_reservation.reservation.infrastructure.scheduling;

import io.github.nemm.study.inventory_reservation.reservation.application.ReservationExpirationService;
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
