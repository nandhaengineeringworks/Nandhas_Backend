package com.company.app.enquiry;

import com.company.app.catalogue.entity.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnquiryResponseDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productSlug;
    private String productSku;
    private CategoryType productType;
    private String name;
    private String phone;
    private String email;
    private String companyName;
    private String city;
    private String state;
    private Integer estimatedQuantity;
    private String message;
    private EnquiryStatus status;
    private String internalNotes;
    private String assignedTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static EnquiryResponseDTO fromEntity(Enquiry enquiry) {
        if (enquiry == null) return null;
        return EnquiryResponseDTO.builder()
                .id(enquiry.getId())
                .productId(enquiry.getProduct() != null ? enquiry.getProduct().getId() : null)
                .productName(enquiry.getProductName() != null ? enquiry.getProductName() : (enquiry.getProduct() != null ? enquiry.getProduct().getName() : null))
                .productSlug(enquiry.getProduct() != null ? enquiry.getProduct().getSlug() : null)
                .productSku(enquiry.getProductSku())
                .productType(enquiry.getProductType() != null ? enquiry.getProductType() : (enquiry.getProduct() != null && enquiry.getProduct().getCategory() != null ? enquiry.getProduct().getCategory().getType() : null))
                .name(enquiry.getName())
                .phone(enquiry.getPhone())
                .email(enquiry.getEmail())
                .companyName(enquiry.getCompanyName())
                .city(enquiry.getCity())
                .state(enquiry.getState())
                .estimatedQuantity(enquiry.getEstimatedQuantity())
                .message(enquiry.getMessage())
                .status(enquiry.getStatus())
                .internalNotes(enquiry.getInternalNotes())
                .assignedTo(enquiry.getAssignedTo())
                .createdAt(enquiry.getCreatedAt())
                .updatedAt(enquiry.getUpdatedAt())
                .build();
    }
}
