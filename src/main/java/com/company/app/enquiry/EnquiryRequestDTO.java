package com.company.app.enquiry;

import com.company.app.catalogue.entity.CategoryType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnquiryRequestDTO {
    private Long productId;
    private String productName;
    private String productSku;
    private CategoryType productType;

    @NotBlank(message = "Customer name is required")
    private String name;

    @NotBlank(message = "Phone number is required")
    private String phone;

    private String email;
    private String companyName;
    private String city;
    private String state;
    private Integer estimatedQuantity;
    private String message;
}
