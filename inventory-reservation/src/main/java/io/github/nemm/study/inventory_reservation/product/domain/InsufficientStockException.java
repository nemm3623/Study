package io.github.nemm.study.inventory_reservation.product.domain;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException() {
        super("not enough available stock");
    }
}
