package io.github.nemm.study.inventory_reservation.product.api;

import io.github.nemm.study.inventory_reservation.product.application.StockResult;

public record StockResponse(
        String productId,
        int totalStock,
        int availableStock,
        int reservedStock,
        int soldStock
) {
    public static StockResponse from(StockResult result) {
        return new StockResponse(result.productId(), result.totalStock(),
                result.availableStock(), result.reservedStock(), result.soldStock());
    }
}
