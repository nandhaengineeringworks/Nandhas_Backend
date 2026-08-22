package com.company.app.catalogue.dto;

import com.company.app.catalogue.entity.CategoryType;
import com.company.app.catalogue.entity.Product;
import com.company.app.catalogue.entity.ProductImage;
import com.company.app.catalogue.entity.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummaryDTO {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String categorySlug;
    private CategoryType categoryType;
    private String name;
    private String slug;
    private String sku;
    private String modelNumber;
    private String productType;
    private String brand;
    private String availability;
    private String shortDesc;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private BigDecimal startingFromPrice;
    private String priceMode;
    private Boolean isQuoteOnly;
    private String primaryImageUrl;
    private ProductStatus status;
    private Boolean isFeatured;
    private Integer sortOrder;
    @Builder.Default
    private Map<String, String> keySpecs = new HashMap<>();

    public static ProductSummaryDTO fromEntity(Product product) {
        if (product == null) return null;

        Map<String, String> specsMap = new HashMap<>();
        if (product.getSpecs() != null) {
            product.getSpecs().stream()
                    .limit(5)
                    .forEach(s -> specsMap.put(s.getSpecKey(), s.getSpecValue()));
        }

        String primaryImg = product.getPrimaryImageUrl();
        if (primaryImg != null && primaryImg.startsWith("blob:")) {
            primaryImg = null;
        }

        if (primaryImg == null && product.getImages() != null) {
            primaryImg = product.getImages().stream()
                    .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                    .map(ProductImage::getImageUrl)
                    .filter(url -> url != null && !url.startsWith("blob:"))
                    .findFirst()
                    .orElseGet(() -> product.getImages().stream()
                            .map(ProductImage::getImageUrl)
                            .filter(url -> url != null && !url.startsWith("blob:"))
                            .findFirst()
                            .orElse(null));
        }

        return ProductSummaryDTO.builder()
                .id(product.getId())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .categorySlug(product.getCategory() != null ? product.getCategory().getSlug() : null)
                .categoryType(product.getCategory() != null ? product.getCategory().getType() : null)
                .name(product.getName())
                .slug(product.getSlug())
                .sku(product.getSku())
                .modelNumber(product.getModelNumber())
                .productType(product.getProductType() != null ? product.getProductType() : "Machinery")
                .brand(product.getBrand() != null ? product.getBrand() : "Nandhas")
                .availability(product.getAvailability() != null ? product.getAvailability() : "MADE_TO_ORDER")
                .shortDesc(product.getShortDesc())
                .price(product.getPrice())
                .compareAtPrice(product.getCompareAtPrice())
                .startingFromPrice(product.getStartingFromPrice())
                .priceMode(product.getPriceMode() != null ? product.getPriceMode() : "QUOTE_ONLY")
                .isQuoteOnly(product.getIsQuoteOnly())
                .primaryImageUrl(primaryImg)
                .status(product.getStatus())
                .isFeatured(product.getIsFeatured())
                .sortOrder(product.getSortOrder())
                .keySpecs(specsMap)
                .build();
    }
}
