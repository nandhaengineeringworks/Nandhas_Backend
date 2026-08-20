package com.company.app.banner;

import com.company.app.catalogue.entity.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {
    List<Banner> findByIsActiveTrueOrderBySortOrderAsc();
    List<Banner> findByTypeAndIsActiveTrueOrderBySortOrderAsc(CategoryType type);
}
