package com.company.app.catalogue.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "product_variants")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    @Column(nullable = false)
    private String variantName; // e.g. "Single Phase (220V)" or "8ft x 4ft - Walnut"

    private String sku;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Builder.Default
    private Integer stockQty = 10;

    @Column(length = 1000)
    private String imageUrl;

    @Builder.Default
    private Boolean isDefault = false;
}
