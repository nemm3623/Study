package io.github.nemm.study.invertory_reservation.reservation;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/products/{productId}/reservations")
    public ResponseEntity<ReservationResponse> create(@PathVariable String productId,
                                                       @Valid @RequestBody CreateReservationRequest request) {
        ReservationService.CreationResult result = reservationService.create(productId, request);
        return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(result.response());
    }

    @GetMapping("/reservations/{reservationId}")
    public ReservationResponse get(@PathVariable String reservationId) {
        return reservationService.get(reservationId);
    }

    @PostMapping("/reservations/{reservationId}/confirm")
    public ReservationResponse confirm(@PathVariable String reservationId) {
        return reservationService.confirm(reservationId);
    }

    @PostMapping("/reservations/{reservationId}/cancel")
    public ReservationResponse cancel(@PathVariable String reservationId) {
        return reservationService.cancel(reservationId);
    }
}
