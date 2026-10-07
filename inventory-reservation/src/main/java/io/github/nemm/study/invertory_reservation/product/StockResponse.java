package io.github.nemm.study.invertory_reservation.product;

public record StockResponse(
        String productId,
        int totalStock,
        int availableStock,
        int reservedStock,
        int soldStock
) {
    public static StockResponse from(Product product) {
        return new StockResponse(product.getProductId(), product.getTotalStock(),
                product.getAvailableStock(), product.getReservedStock(), product.getSoldStock());
    }
}
