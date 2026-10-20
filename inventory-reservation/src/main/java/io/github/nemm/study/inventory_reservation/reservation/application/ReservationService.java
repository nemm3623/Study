package io.github.nemm.study.inventory_reservation.reservation.application;

import io.github.nemm.study.inventory_reservation.common.error.BusinessException;
import io.github.nemm.study.inventory_reservation.common.error.CommonErrorCode;
import io.github.nemm.study.inventory_reservation.product.domain.Product;
import io.github.nemm.study.inventory_reservation.product.domain.ProductRepository;
import io.github.nemm.study.inventory_reservation.product.error.ProductErrorCode;
import io.github.nemm.study.inventory_reservation.reservation.domain.Reservation;
import io.github.nemm.study.inventory_reservation.reservation.domain.ReservationRepository;
import io.github.nemm.study.inventory_reservation.reservation.domain.ReservationStatus;
import io.github.nemm.study.inventory_reservation.reservation.error.ReservationErrorCode;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ProductRepository products;
    private final ReservationRepository reservations;
    private final ReservationExpirationService expirationService;
    private final Clock clock;

    @Transactional
    public ReservationCreationResult create(String productId, CreateReservationCommand request) {
        if (productId == null || productId.isBlank() || request == null
                || request.customerId() == null || request.customerId().isBlank()
                || request.requestId() == null || request.requestId().isBlank()
                || request.quantity() == null || request.quantity() <= 0) {
            throw new BusinessException(CommonErrorCode.INVALID_REQUEST);
        }

        expirationService.expireDue();

        Product product = products.findByProductId(productId)
                .orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND));
        var existing = reservations.findByProductAndRequestId(product, request.requestId());
        if (existing.isPresent()) {
            Reservation reservation = existing.get();
            if (!reservation.getCustomerId().equals(request.customerId())
                    || reservation.getQuantity() != request.quantity()) {
                throw new BusinessException(ReservationErrorCode.DUPLICATE_REQUEST_MISMATCH);
            }
            return new ReservationCreationResult(ReservationResult.from(reservation), false);
        }

        product.reserve(request.quantity());
        Reservation reservation = reservations.save(Reservation.create(product, request.customerId(),
                request.quantity(), request.requestId(), clock.instant()));
        return new ReservationCreationResult(ReservationResult.from(reservation), true);
    }

    @Transactional(readOnly = true)
    public ReservationResult get(String reservationId) {
        expirationService.expireDue();
        return ReservationResult.from(findReservation(reservationId));
    }

    @Transactional
    public ReservationResult confirm(String reservationId) {
        expirationService.expireDue();
        Reservation reservation = findReservation(reservationId);
        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            return ReservationResult.from(reservation);
        }
        reservation.confirm();
        return ReservationResult.from(reservation);
    }

    @Transactional
    public ReservationResult cancel(String reservationId) {
        expirationService.expireDue();
        Reservation reservation = findReservation(reservationId);
        if (reservation.getStatus() == ReservationStatus.CANCELED) {
            return ReservationResult.from(reservation);
        }
        reservation.cancel();
        return ReservationResult.from(reservation);
    }

    private Reservation findReservation(String reservationId) {
        if (reservationId == null || reservationId.isBlank()) {
            throw new BusinessException(CommonErrorCode.INVALID_REQUEST);
        }
        return reservations.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ReservationErrorCode.RESERVATION_NOT_FOUND));
    }
}
