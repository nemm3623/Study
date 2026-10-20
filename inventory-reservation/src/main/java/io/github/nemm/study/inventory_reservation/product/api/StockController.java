package io.github.nemm.study.inventory_reservation.product.api;

import io.github.nemm.study.inventory_reservation.product.application.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class StockController {

    private final StockService stockService;

    @GetMapping("/{productId}/stock")
    public StockResponse getStock(@PathVariable String productId) {
        return StockResponse.from(stockService.getStock(productId));
    }
}
