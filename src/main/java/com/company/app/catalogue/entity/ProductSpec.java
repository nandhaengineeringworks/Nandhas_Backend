package com.company.app.catalogue.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_specs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSpec {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    @Column(nullable = false)
    private String specKey;

    @Column(nullable = false, length = 1000)
    private String specValue;

    private String specGroup; // e.g. "General", "Electrical", "Dimensions", "Capacity"

    @Builder.Default
    private Integer sortOrder = 0;
}
