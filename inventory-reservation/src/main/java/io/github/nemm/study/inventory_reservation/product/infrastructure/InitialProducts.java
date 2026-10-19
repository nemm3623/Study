package io.github.nemm.study.inventory_reservation.product.infrastructure;

import io.github.nemm.study.inventory_reservation.product.domain.Product;
import io.github.nemm.study.inventory_reservation.product.domain.ProductRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class InitialProducts implements ApplicationRunner {

    private final ProductRepository products;

    public InitialProducts(ProductRepository products) {
        this.products = products;
    }

    @Override
    public void run(ApplicationArguments args) {
        addIfMissing("limited-keyboard", 10);
        addIfMissing("limited-monitor", 3);
    }

    private void addIfMissing(String productId, int totalStock) {
        if (products.findByProductId(productId).isEmpty()) {
            products.save(Product.initial(productId, totalStock));
        }
    }
}
