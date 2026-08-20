package com.company.app.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {
    private Long id;
    private String orderNumber;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String companyName;
    private String shippingAddress;
    private String city;
    private String state;
    private String postalCode;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private String paymentMethod;
    private String transactionId;
    private String notes;
    private List<OrderItemResponseDTO> items;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponseDTO {
        private Long id;
        private Long productId;
        private String productName;
        private String productSku;
        private String variantName;
        private String imageUrl;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal totalPrice;

        public static OrderItemResponseDTO fromEntity(OrderItem item) {
            return OrderItemResponseDTO.builder()
                    .id(item.getId())
                    .productId(item.getProductId())
                    .productName(item.getProductName())
                    .productSku(item.getProductSku())
                    .variantName(item.getVariantName())
                    .imageUrl(item.getImageUrl())
                    .unitPrice(item.getUnitPrice())
                    .quantity(item.getQuantity())
                    .totalPrice(item.getTotalPrice())
                    .build();
        }
    }

    public static OrderResponseDTO fromEntity(Order order) {
        if (order == null) return null;
        return OrderResponseDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .customerPhone(order.getCustomerPhone())
                .companyName(order.getCompanyName())
                .shippingAddress(order.getShippingAddress())
                .city(order.getCity())
                .state(order.getState())
                .postalCode(order.getPostalCode())
                .subtotal(order.getSubtotal())
                .taxAmount(order.getTaxAmount())
                .shippingFee(order.getShippingFee())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .paymentMethod(order.getPaymentMethod())
                .transactionId(order.getTransactionId())
                .notes(order.getNotes())
                .items(order.getItems() != null ?
                        order.getItems().stream().map(OrderItemResponseDTO::fromEntity).collect(Collectors.toList()) :
                        List.of())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
