package com.company.app.catalogue.repository;

import com.company.app.catalogue.entity.CategoryType;
import com.company.app.catalogue.entity.Product;
import com.company.app.catalogue.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Optional<Product> findBySlug(String slug);
    boolean existsBySlug(String slug);

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = :status AND p.category.type = :type")
    Page<Product> findByStatusAndCategoryType(@Param("status") ProductStatus status, @Param("type") CategoryType type, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = :status AND (p.category.id = :categoryId OR p.category.parent.id = :categoryId)")
    Page<Product> findByStatusAndCategoryId(@Param("status") ProductStatus status, @Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = 'PUBLISHED' AND p.isFeatured = true AND p.category.type = :type ORDER BY p.sortOrder ASC")
    List<Product> findFeaturedByType(@Param("type") CategoryType type);

    @Query("SELECT p FROM Product p WHERE p.status = 'PUBLISHED' AND p.isFeatured = true ORDER BY p.sortOrder ASC")
    List<Product> findFeaturedAll();

    @Query("SELECT p FROM Product p WHERE p.status = 'PUBLISHED' AND p.category.id = :categoryId AND p.id <> :excludeId")
    List<Product> findRelatedProducts(@Param("categoryId") Long categoryId, @Param("excludeId") Long excludeId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = 'PUBLISHED' AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.shortDesc) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchPublished(@Param("keyword") String keyword, Pageable pageable);

    long countByStatus(ProductStatus status);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.type = :type")
    long countByCategoryType(@Param("type") CategoryType type);
}
