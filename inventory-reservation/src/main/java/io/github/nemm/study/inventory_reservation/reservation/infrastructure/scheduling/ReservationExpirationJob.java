package io.github.nemm.study.inventory_reservation.reservation.infrastructure.scheduling;

import io.github.nemm.study.inventory_reservation.reservation.application.ReservationExpirationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationExpirationJob {

    private final ReservationExpirationService expirationService;

    @Scheduled(fixedDelay = 1000)
    public void expireDue() {
        expirationService.expireDue();
    }
}
