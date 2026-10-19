package io.github.nemm.study.inventory_reservation.product.application;

import io.github.nemm.study.inventory_reservation.product.domain.Product;

public record StockResult(
        String productId,
        int totalStock,
        int availableStock,
        int reservedStock,
        int soldStock
) {
    public static StockResult from(Product product) {
        return new StockResult(product.getProductId(), product.getTotalStock(),
                product.getAvailableStock(), product.getReservedStock(), product.getSoldStock());
    }
}
