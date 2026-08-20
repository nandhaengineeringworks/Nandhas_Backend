package com.company.app.catalogue.repository;

import com.company.app.catalogue.entity.ProductSpec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSpecRepository extends JpaRepository<ProductSpec, Long> {
    List<ProductSpec> findByProductIdOrderBySortOrderAsc(Long productId);
    void deleteByProductId(Long productId);
}
