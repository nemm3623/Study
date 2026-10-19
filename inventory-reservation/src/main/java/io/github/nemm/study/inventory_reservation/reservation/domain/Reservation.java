package io.github.nemm.study.inventory_reservation.reservation.domain;

import io.github.nemm.study.inventory_reservation.product.domain.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reservations", uniqueConstraints =
        @UniqueConstraint(columnNames = {"product_pk", "request_id"}))
public class Reservation {

    @Id
    @Column(name = "reservation_id", nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_pk", nullable = false, updatable = false)
    private Product product;

    @Column(name = "customer_id", nullable = false, updatable = false)
    private String customerId;

    @Column(name = "request_id", nullable = false, updatable = false)
    private String requestId;

    @Column(nullable = false, updatable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    protected Reservation() {
    }

    public static Reservation create(Product product, String customerId, int quantity,
                                     String requestId, Instant now) {
        Reservation reservation = new Reservation();
        reservation.id = UUID.randomUUID().toString();
        reservation.product = product;
        reservation.customerId = customerId;
        reservation.quantity = quantity;
        reservation.requestId = requestId;
        reservation.status = ReservationStatus.RESERVED;
        reservation.createdAt = now;
        reservation.expiresAt = now.plusSeconds(120);
        return reservation;
    }

    public String getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getRequestId() {
        return requestId;
    }

    public int getQuantity() {
        return quantity;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void confirm() {
        requireReserved();
        product.confirmReserved(quantity);
        status = ReservationStatus.CONFIRMED;
    }

    public void cancel() {
        requireReserved();
        product.releaseReserved(quantity);
        status = ReservationStatus.CANCELED;
    }

    public void expire(Instant now) {
        requireReserved();
        if (now.isBefore(expiresAt)) {
            throw new IllegalStateException("reservation has not expired");
        }
        product.releaseReserved(quantity);
        status = ReservationStatus.EXPIRED;
    }

    private void requireReserved() {
        if (status != ReservationStatus.RESERVED) {
            throw new IllegalStateException("reservation is not reserved");
        }
    }
}
