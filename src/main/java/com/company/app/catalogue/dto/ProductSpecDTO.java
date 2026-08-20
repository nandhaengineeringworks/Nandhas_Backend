package com.company.app.catalogue.dto;

import com.company.app.catalogue.entity.ProductSpec;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSpecDTO {
    private Long id;
    private String specKey;
    private String specValue;
    private String specGroup;
    private Integer sortOrder;

    public static ProductSpecDTO fromEntity(ProductSpec spec) {
        if (spec == null) return null;
        return ProductSpecDTO.builder()
                .id(spec.getId())
                .specKey(spec.getSpecKey())
                .specValue(spec.getSpecValue())
                .specGroup(spec.getSpecGroup())
                .sortOrder(spec.getSortOrder())
                .build();
    }
}
