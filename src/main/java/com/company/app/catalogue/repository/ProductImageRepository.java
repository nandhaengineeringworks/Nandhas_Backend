package com.company.app.catalogue.repository;

import com.company.app.catalogue.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductIdOrderBySortOrderAsc(Long productId);
    void deleteByProductId(Long productId);
}
