package com.company.app.catalogue.dto;

import com.company.app.catalogue.entity.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotBlank(message = "Product name is required")
    private String name;

    private String slug;
    private String sku;
    private String modelNumber;

    private String shortDesc;
    private String description;
    private String productType; // Machinery, Material, Spare Part, Accessory, Service

    private String brand;
    private String manufacturer;
    private String countryOfOrigin;

    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private BigDecimal startingFromPrice;
    private String priceMode;
    private BigDecimal gstPercentage;

    @Builder.Default
    private Boolean isQuoteOnly = true;

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

    @Builder.Default
    private ProductStatus status = ProductStatus.PUBLISHED;

    @Builder.Default
    private Boolean isFeatured = false;

    @Builder.Default
    private Integer sortOrder = 0;

    @Builder.Default
    private List<ProductImageDTO> images = new ArrayList<>();

    @Builder.Default
    private List<ProductSpecDTO> specs = new ArrayList<>();

    @Builder.Default
    private List<ProductVariantDTO> variants = new ArrayList<>();
}
