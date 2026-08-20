package com.company.app.catalogue.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1. Basic Information
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnoreProperties({"products", "subCategories", "parent"})
    private Category category;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    private String sku;
    private String modelNumber;

    @Column(length = 500)
    private String shortDesc;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    private String productType = "Machinery"; // Machinery, Material, Spare Part, Accessory, Service

    // 2. Brand & Manufacturer
    @Builder.Default
    private String brand = "Nandhas";
    private String manufacturer;
    @Builder.Default
    private String countryOfOrigin = "India";

    // 3. Pricing
    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Column(precision = 12, scale = 2)
    private BigDecimal compareAtPrice;

    @Column(precision = 12, scale = 2)
    private BigDecimal startingFromPrice;

    @Builder.Default
    private String priceMode = "QUOTE_ONLY"; // FIXED, QUOTE_ONLY, STARTING_FROM

    @Builder.Default
    @Column(precision = 5, scale = 2)
    private BigDecimal gstPercentage = new BigDecimal("18.00");

    @Builder.Default
    @Column(nullable = false)
    private Boolean isQuoteOnly = true;

    // 4. Media & Documents
    @Column(length = 1000)
    private String primaryImageUrl;

    @Column(length = 1000)
    private String brochureUrl;

    @Column(length = 1000)
    private String technicalDrawingUrl;

    @Column(length = 1000)
    private String videoUrl;

    @Column(length = 1000)
    private String view360Url;

    // 5. Applications
    @Lob
    @Column(columnDefinition = "TEXT")
    private String applicationsJson; // Array of application tags

    // 6. Inventory & Availability
    @Builder.Default
    private String availability = "MADE_TO_ORDER"; // IN_STOCK, MADE_TO_ORDER, PRE_ORDER, OUT_OF_STOCK, CUSTOM
    private Integer stockQuantity;
    @Builder.Default
    private Integer minOrderQuantity = 1;
    private String productionLeadTime; // e.g. "6-8 Weeks"

    // 7. Shipping & Installation
    private String productWeight; // e.g. "850 KG"
    private String packageDimensions; // e.g. "2100 x 1400 x 1800 mm"
    private String deliveryTime; // e.g. "2-3 Weeks"
    @Builder.Default
    private Boolean isInstallationAvailable = true;
    private String installationCharges; // e.g. "Free Commissioning" or "Contact for Quote"

    // 8. Warranty & Service
    @Builder.Default
    private String warrantyPeriod = "1 Year Comprehensive";
    @Builder.Default
    private String serviceLocations = "Pan India";
    @Builder.Default
    private Boolean isSparePartsAvailable = true;
    @Builder.Default
    private Boolean isAmcAvailable = true;

    // 9. SEO
    private String seoTitle;
    @Column(length = 500)
    private String seoDescription;
    private String seoKeywords;

    // Status & Organization
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProductStatus status = ProductStatus.PUBLISHED;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isFeatured = false;

    @Builder.Default
    private Integer sortOrder = 0;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<ProductSpec> specs = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void addImage(ProductImage image) {
        images.add(image);
        image.setProduct(this);
    }

    public void addSpec(ProductSpec spec) {
        specs.add(spec);
        spec.setProduct(this);
    }

    public void addVariant(ProductVariant variant) {
        variants.add(variant);
        variant.setProduct(this);
    }
}
