package io.github.nemm.study.inventory_reservation.product.infrastructure.persistence;

import io.github.nemm.study.inventory_reservation.product.domain.Product;
import io.github.nemm.study.inventory_reservation.product.domain.ProductRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaProductRepositoryAdapter implements ProductRepository {

    private final JpaProductRepository jpaRepository;

    public JpaProductRepositoryAdapter(JpaProductRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Product> findByProductId(String productId) {
        return jpaRepository.findByProductId(productId);
    }

    @Override
    public Product save(Product product) {
        return jpaRepository.save(product);
    }
}
