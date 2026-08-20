package com.company.app.catalogue.dto;

import com.company.app.catalogue.entity.ProductImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageDTO {
    private Long id;
    private String imageUrl;
    private String altText;
    private Integer sortOrder;
    private Boolean isPrimary;

    public static ProductImageDTO fromEntity(ProductImage image) {
        if (image == null) return null;
        return ProductImageDTO.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .altText(image.getAltText())
                .sortOrder(image.getSortOrder())
                .isPrimary(image.getIsPrimary())
                .build();
    }
}
