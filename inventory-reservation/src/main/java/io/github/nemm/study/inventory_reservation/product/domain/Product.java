package io.github.nemm.study.inventory_reservation.product.domain;

import io.github.nemm.study.inventory_reservation.common.error.BusinessException;
import io.github.nemm.study.inventory_reservation.product.error.ProductErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "products", uniqueConstraints = @UniqueConstraint(columnNames = "product_id"))
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false, updatable = false)
    private String productId;

    @Column(nullable = false)
    private int totalStock;

    @Column(nullable = false)
    private int availableStock;

    @Column(nullable = false)
    private int reservedStock;

    @Column(nullable = false)
    private int soldStock;

    public static Product initial(String productId, int totalStock) {
        Product product = new Product();
        product.productId = productId;
        product.totalStock = totalStock;
        product.availableStock = totalStock;
        return product;
    }

    public Long getId() {
        return id;
    }

    public String getProductId() {
        return productId;
    }

    public int getTotalStock() {
        return totalStock;
    }

    public int getAvailableStock() {
        return availableStock;
    }

    public int getReservedStock() {
        return reservedStock;
    }

    public int getSoldStock() {
        return soldStock;
    }

    public void reserve(int quantity) {
        if (quantity > availableStock) {
            throw new BusinessException(ProductErrorCode.INSUFFICIENT_STOCK, "not enough available stock");
        }
        availableStock -= quantity;
        reservedStock += quantity;
    }

    public void confirmReserved(int quantity) {
        requireReserved(quantity);
        reservedStock -= quantity;
        soldStock += quantity;
    }

    public void releaseReserved(int quantity) {
        requireReserved(quantity);
        reservedStock -= quantity;
        availableStock += quantity;
    }

    private void requireReserved(int quantity) {
        if (quantity <= 0 || quantity > reservedStock) {
            throw new IllegalStateException("reserved stock is inconsistent");
        }
    }
}
