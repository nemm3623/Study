package io.github.nemm.study.inventory_reservation.product.domain;

import java.util.Optional;

public interface ProductRepository {
    Optional<Product> findByProductId(String productId);

    Product save(Product product);
}
