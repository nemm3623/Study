package io.github.nemm.study.invertory_reservation.product;

import io.github.nemm.study.invertory_reservation.support.ApiException;
import io.github.nemm.study.invertory_reservation.reservation.ReservationExpirationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {

    private final ProductRepository products;
    private final ReservationExpirationService expirationService;

    public StockService(ProductRepository products, ReservationExpirationService expirationService) {
        this.products = products;
        this.expirationService = expirationService;
    }

    @Transactional(readOnly = true)
    public StockResponse getStock(String productId) {
        expirationService.expireDue();
        Product product = products.findByProductId(productId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "product not found"));
        return StockResponse.from(product);
    }
}
