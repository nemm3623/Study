package io.github.nemm.study.inventory_reservation.product.infrastructure.persistence;

import io.github.nemm.study.inventory_reservation.product.domain.Product;
import io.github.nemm.study.inventory_reservation.product.domain.ProductRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaProductRepositoryAdapter implements ProductRepository {

    private final JpaProductRepository jpaRepository;

    @Override
    public Optional<Product> findByProductId(String productId) {
        return jpaRepository.findByProductId(productId);
    }

    @Override
    public Product save(Product product) {
        return jpaRepository.save(product);
    }
}
