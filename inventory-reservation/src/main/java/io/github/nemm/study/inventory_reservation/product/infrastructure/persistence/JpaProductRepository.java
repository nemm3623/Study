package io.github.nemm.study.inventory_reservation.product.infrastructure.persistence;

import io.github.nemm.study.inventory_reservation.product.domain.Product;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByProductId(String productId);
}
