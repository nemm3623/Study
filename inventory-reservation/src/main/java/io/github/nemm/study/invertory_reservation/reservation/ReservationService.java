package io.github.nemm.study.invertory_reservation.reservation;

import io.github.nemm.study.invertory_reservation.product.Product;
import io.github.nemm.study.invertory_reservation.product.ProductRepository;
import io.github.nemm.study.invertory_reservation.support.ApiException;
import java.time.Clock;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationService {

    private final ProductRepository products;
    private final ReservationRepository reservations;
    private final ReservationExpirationService expirationService;
    private final Clock clock;

    public ReservationService(ProductRepository products, ReservationRepository reservations,
                              ReservationExpirationService expirationService, Clock clock) {
        this.products = products;
        this.reservations = reservations;
        this.expirationService = expirationService;
        this.clock = clock;
    }

    @Transactional
    public CreationResult create(String productId, CreateReservationRequest request) {
        if (productId == null || productId.isBlank() || request == null
                || request.customerId() == null || request.customerId().isBlank()
                || request.requestId() == null || request.requestId().isBlank()
                || request.quantity() == null || request.quantity() <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "invalid request");
        }

        expirationService.expireDue();

        Product product = products.findByProductId(productId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "product not found"));
        var existing = reservations.findByProductAndRequestId(product, request.requestId());
        if (existing.isPresent()) {
            Reservation reservation = existing.get();
            if (!reservation.getCustomerId().equals(request.customerId())
                    || reservation.getQuantity() != request.quantity()) {
                throw new ApiException(HttpStatus.CONFLICT, "DUPLICATE_REQUEST_MISMATCH",
                        "requestId was used with different reservation details");
            }
            return new CreationResult(ReservationResponse.from(reservation), false);
        }

        product.reserve(request.quantity());
        Reservation reservation = reservations.save(Reservation.create(product, request.customerId(),
                request.quantity(), request.requestId(), clock.instant()));
        return new CreationResult(ReservationResponse.from(reservation), true);
    }

    @Transactional(readOnly = true)
    public ReservationResponse get(String reservationId) {
        expirationService.expireDue();
        return ReservationResponse.from(findReservation(reservationId));
    }

    @Transactional
    public ReservationResponse confirm(String reservationId) {
        expirationService.expireDue();
        Reservation reservation = findReservation(reservationId);
        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            return ReservationResponse.from(reservation);
        }
        requireReserved(reservation);
        reservation.confirm();
        return ReservationResponse.from(reservation);
    }

    @Transactional
    public ReservationResponse cancel(String reservationId) {
        expirationService.expireDue();
        Reservation reservation = findReservation(reservationId);
        if (reservation.getStatus() == ReservationStatus.CANCELED) {
            return ReservationResponse.from(reservation);
        }
        requireReserved(reservation);
        reservation.cancel();
        return ReservationResponse.from(reservation);
    }

    private Reservation findReservation(String reservationId) {
        if (reservationId == null || reservationId.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "invalid request");
        }
        return reservations.findById(reservationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "RESERVATION_NOT_FOUND", "reservation not found"));
    }

    private void requireReserved(Reservation reservation) {
        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new ApiException(HttpStatus.CONFLICT, "INVALID_RESERVATION_STATE",
                    "reservation cannot change to the requested state");
        }
    }

    public record CreationResult(ReservationResponse response, boolean created) {
    }
}
