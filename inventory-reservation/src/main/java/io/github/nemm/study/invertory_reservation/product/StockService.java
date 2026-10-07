package io.github.nemm.study.invertory_reservation.product;

import io.github.nemm.study.invertory_reservation.support.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {

    private final ProductRepository products;

    public StockService(ProductRepository products) {
        this.products = products;
    }

    @Transactional(readOnly = true)
    public StockResponse getStock(String productId) {
        Product product = products.findByProductId(productId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "product not found"));
        return StockResponse.from(product);
    }
}
