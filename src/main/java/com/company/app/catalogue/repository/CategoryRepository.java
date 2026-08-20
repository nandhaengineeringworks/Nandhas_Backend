package com.company.app.catalogue.repository;

import com.company.app.catalogue.entity.Category;
import com.company.app.catalogue.entity.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlug(String slug);
    boolean existsBySlug(String slug);
    List<Category> findByTypeAndIsActiveTrueOrderBySortOrderAsc(CategoryType type);
    List<Category> findByParentIsNullAndIsActiveTrueOrderBySortOrderAsc();
    List<Category> findByParentIsNullAndTypeAndIsActiveTrueOrderBySortOrderAsc(CategoryType type);
    List<Category> findByParentIdAndIsActiveTrueOrderBySortOrderAsc(Long parentId);

    @Query("SELECT c FROM Category c WHERE c.parent IS NULL ORDER BY c.sortOrder ASC")
    List<Category> findAllRootCategories();
}
