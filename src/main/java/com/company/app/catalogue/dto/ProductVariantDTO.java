package com.company.app.catalogue.dto;

import com.company.app.catalogue.entity.ProductVariant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantDTO {
    private Long id;
    private String variantName;
    private String sku;
    private BigDecimal price;
    private Integer stockQty;
    private String imageUrl;
    private Boolean isDefault;

    public static ProductVariantDTO fromEntity(ProductVariant variant) {
        if (variant == null) return null;
        return ProductVariantDTO.builder()
                .id(variant.getId())
                .variantName(variant.getVariantName())
                .sku(variant.getSku())
                .price(variant.getPrice())
                .stockQty(variant.getStockQty())
                .imageUrl(variant.getImageUrl())
                .isDefault(variant.getIsDefault())
                .build();
    }
}
