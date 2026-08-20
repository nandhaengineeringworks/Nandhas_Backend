package com.company.app.catalogue.dto;

import com.company.app.catalogue.entity.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFilterRequest {
    private CategoryType type;
    private Long categoryId;
    private String categorySlug;
    private String search;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean isFeatured;
    private String sortBy; // "price_asc", "price_desc", "name_asc", "newest"
    @Builder.Default
    private int page = 0;
    @Builder.Default
    private int size = 12;
}
