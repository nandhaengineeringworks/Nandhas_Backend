package com.company.app.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDTO {

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Email is required")
    private String customerEmail;

    @NotBlank(message = "Phone number is required")
    private String customerPhone;

    private String companyName;

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    private String city;
    private String state;
    private String postalCode;
    private String paymentMethod; // "RAZORPAY", "BANK_TRANSFER", "COD"
    private String notes;

    @NotEmpty(message = "Order must contain at least one item")
    private List<OrderItemDTO> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDTO {
        private Long productId;
        private String productName;
        private String productSku;
        private String variantName;
        private String imageUrl;
        private BigDecimal unitPrice;
        private Integer quantity;
    }
}
