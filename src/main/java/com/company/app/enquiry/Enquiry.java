package com.company.app.enquiry;

import com.company.app.catalogue.entity.CategoryType;
import com.company.app.catalogue.entity.Product;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "enquiries")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Enquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @JsonIgnoreProperties({"description", "specs", "images", "variants", "category"})
    private Product product;

    private String productName;

    private String productSku;

    @Enumerated(EnumType.STRING)
    private CategoryType productType; // MACHINERY or INTERIOR

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    private String email;

    private String companyName;

    private String city;

    private String state;

    private Integer estimatedQuantity;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EnquiryStatus status = EnquiryStatus.NEW;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String internalNotes;

    private String assignedTo;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
