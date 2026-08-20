package com.company.app.catalogue.dto;

import com.company.app.catalogue.entity.CategoryType;
import com.company.app.catalogue.entity.Product;
import com.company.app.catalogue.entity.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailDTO {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String categorySlug;
    private CategoryType categoryType;

    private String name;
    private String slug;
    private String sku;
    private String modelNumber;
    private String shortDesc;
    private String description;
    private String productType;

    private String brand;
    private String manufacturer;
    private String countryOfOrigin;

    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private BigDecimal startingFromPrice;
    private String priceMode;
    private BigDecimal gstPercentage;
    private Boolean isQuoteOnly;

    private String primaryImageUrl;
    private String brochureUrl;
    private String technicalDrawingUrl;
    private String videoUrl;
    private String view360Url;

    private String applicationsJson;

    private String availability;
    private Integer stockQuantity;
    private Integer minOrderQuantity;
    private String productionLeadTime;

    private String productWeight;
    private String packageDimensions;
    private String deliveryTime;
    private Boolean isInstallationAvailable;
    private String installationCharges;

    private String warrantyPeriod;
    private String serviceLocations;
    private Boolean isSparePartsAvailable;
    private Boolean isAmcAvailable;

    private String seoTitle;
    private String seoDescription;
    private String seoKeywords;

    private ProductStatus status;
    private Boolean isFeatured;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    private List<ProductImageDTO> images = new ArrayList<>();

    @Builder.Default
    private List<ProductSpecDTO> specs = new ArrayList<>();

    @Builder.Default
    private List<ProductVariantDTO> variants = new ArrayList<>();

    public static ProductDetailDTO fromEntity(Product product) {
        if (product == null) return null;

        String primaryImg = product.getPrimaryImageUrl();
        if (primaryImg == null && product.getImages() != null && !product.getImages().isEmpty()) {
            primaryImg = product.getImages().get(0).getImageUrl();
        }

        return ProductDetailDTO.builder()
                .id(product.getId())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .categorySlug(product.getCategory() != null ? product.getCategory().getSlug() : null)
                .categoryType(product.getCategory() != null ? product.getCategory().getType() : null)
                .name(product.getName())
                .slug(product.getSlug())
                .sku(product.getSku())
                .modelNumber(product.getModelNumber())
                .shortDesc(product.getShortDesc())
                .description(product.getDescription())
                .productType(product.getProductType() != null ? product.getProductType() : "Machinery")
                .brand(product.getBrand() != null ? product.getBrand() : "Nandhas")
                .manufacturer(product.getManufacturer())
                .countryOfOrigin(product.getCountryOfOrigin() != null ? product.getCountryOfOrigin() : "India")
                .price(product.getPrice())
                .compareAtPrice(product.getCompareAtPrice())
                .startingFromPrice(product.getStartingFromPrice())
                .priceMode(product.getPriceMode() != null ? product.getPriceMode() : "QUOTE_ONLY")
                .gstPercentage(product.getGstPercentage() != null ? product.getGstPercentage() : new BigDecimal("18.00"))
                .isQuoteOnly(product.getIsQuoteOnly())
                .primaryImageUrl(primaryImg)
                .brochureUrl(product.getBrochureUrl())
                .technicalDrawingUrl(product.getTechnicalDrawingUrl())
                .videoUrl(product.getVideoUrl())
                .view360Url(product.getView360Url())
                .applicationsJson(product.getApplicationsJson())
                .availability(product.getAvailability() != null ? product.getAvailability() : "MADE_TO_ORDER")
                .stockQuantity(product.getStockQuantity())
                .minOrderQuantity(product.getMinOrderQuantity())
                .productionLeadTime(product.getProductionLeadTime())
                .productWeight(product.getProductWeight())
                .packageDimensions(product.getPackageDimensions())
                .deliveryTime(product.getDeliveryTime())
                .isInstallationAvailable(product.getIsInstallationAvailable())
                .installationCharges(product.getInstallationCharges())
                .warrantyPeriod(product.getWarrantyPeriod() != null ? product.getWarrantyPeriod() : "1 Year Comprehensive")
                .serviceLocations(product.getServiceLocations() != null ? product.getServiceLocations() : "Pan India")
                .isSparePartsAvailable(product.getIsSparePartsAvailable())
                .isAmcAvailable(product.getIsAmcAvailable())
                .seoTitle(product.getSeoTitle())
                .seoDescription(product.getSeoDescription())
                .seoKeywords(product.getSeoKeywords())
                .status(product.getStatus())
                .isFeatured(product.getIsFeatured())
                .sortOrder(product.getSortOrder())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .images(product.getImages() != null ?
                        product.getImages().stream().map(ProductImageDTO::fromEntity).collect(Collectors.toList()) : new ArrayList<>())
                .specs(product.getSpecs() != null ?
                        product.getSpecs().stream().map(ProductSpecDTO::fromEntity).collect(Collectors.toList()) : new ArrayList<>())
                .variants(product.getVariants() != null ?
                        product.getVariants().stream().map(ProductVariantDTO::fromEntity).collect(Collectors.toList()) : new ArrayList<>())
                .build();
    }
}
