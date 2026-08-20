package com.company.app.enquiry;

import com.company.app.catalogue.entity.CategoryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EnquiryRepository extends JpaRepository<Enquiry, Long>, JpaSpecificationExecutor<Enquiry> {
    Page<Enquiry> findByStatusOrderByCreatedAtDesc(EnquiryStatus status, Pageable pageable);
    long countByStatus(EnquiryStatus status);

    @Query("SELECT COUNT(e) FROM Enquiry e WHERE e.productType = :type")
    long countByProductType(@Param("type") CategoryType type);

    @Query("SELECT COUNT(e) FROM Enquiry e WHERE e.createdAt >= :since")
    long countRecentEnquiries(@Param("since") LocalDateTime since);

    List<Enquiry> findTop10ByOrderByCreatedAtDesc();

    @Modifying
    @Query("UPDATE Enquiry e SET e.product = NULL WHERE e.product.id = :productId")
    void detachProductByProductId(@Param("productId") Long productId);
}
