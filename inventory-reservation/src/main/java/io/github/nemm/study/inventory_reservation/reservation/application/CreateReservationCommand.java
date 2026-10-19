package io.github.nemm.study.inventory_reservation.reservation.application;

public record CreateReservationCommand(String customerId, Integer quantity, String requestId) {
}
