package io.github.nemm.study.inventory_reservation.product.application;

import io.github.nemm.study.inventory_reservation.common.application.ApplicationException;
import io.github.nemm.study.inventory_reservation.common.application.ErrorCode;
import io.github.nemm.study.inventory_reservation.product.domain.Product;
import io.github.nemm.study.inventory_reservation.product.domain.ProductRepository;
import io.github.nemm.study.inventory_reservation.reservation.application.ReservationExpirationService;
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
    public StockResult getStock(String productId) {
        if (productId == null || productId.isBlank()) {
            throw new ApplicationException(ErrorCode.INVALID_REQUEST, "invalid request");
        }
        expirationService.expireDue();
        Product product = products.findByProductId(productId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_NOT_FOUND, "product not found"));
        return StockResult.from(product);
    }
}
