package com.company.app.order;

import com.company.app.common.PagedResponse;
import com.company.app.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO dto) {
        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderRequestDTO.OrderItemDTO itemDto : dto.getItems()) {
            BigDecimal linePrice = itemDto.getUnitPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            subtotal = subtotal.add(linePrice);
        }

        BigDecimal taxAmount = subtotal.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP); // 18% GST standard
        BigDecimal shippingFee = BigDecimal.ZERO;
        BigDecimal totalAmount = subtotal.add(taxAmount).add(shippingFee);

        String orderNumber = "ORD-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd")) + "-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .customerName(dto.getCustomerName())
                .customerEmail(dto.getCustomerEmail())
                .customerPhone(dto.getCustomerPhone())
                .companyName(dto.getCompanyName())
                .shippingAddress(dto.getShippingAddress())
                .city(dto.getCity())
                .state(dto.getState())
                .postalCode(dto.getPostalCode())
                .subtotal(subtotal)
                .taxAmount(taxAmount)
                .shippingFee(shippingFee)
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .paymentMethod(dto.getPaymentMethod() != null ? dto.getPaymentMethod() : "ONLINE")
                .notes(dto.getNotes())
                .build();

        for (OrderRequestDTO.OrderItemDTO itemDto : dto.getItems()) {
            BigDecimal totalPrice = itemDto.getUnitPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            OrderItem item = OrderItem.builder()
                    .productId(itemDto.getProductId())
                    .productName(itemDto.getProductName())
                    .productSku(itemDto.getProductSku())
                    .variantName(itemDto.getVariantName())
                    .imageUrl(itemDto.getImageUrl())
                    .unitPrice(itemDto.getUnitPrice())
                    .quantity(itemDto.getQuantity())
                    .totalPrice(totalPrice)
                    .build();
            order.addItem(item);
        }

        Order saved = orderRepository.save(order);
        return OrderResponseDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));
        return OrderResponseDTO.fromEntity(order);
    }

    @Transactional(readOnly = true)
    public PagedResponse<OrderResponseDTO> getOrders(int page, int size, OrderStatus status) {
        Page<Order> orderPage = (status != null) ?
                orderRepository.findByStatusOrderByCreatedAtDesc(status, PageRequest.of(page, size)) :
                orderRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        return PagedResponse.from(orderPage, orderPage.getContent().stream().map(OrderResponseDTO::fromEntity).collect(Collectors.toList()));
    }

    @Transactional
    public OrderResponseDTO updateOrderStatus(Long id, OrderStatus status, PaymentStatus paymentStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        if (status != null) order.setStatus(status);
        if (paymentStatus != null) order.setPaymentStatus(paymentStatus);

        Order saved = orderRepository.save(order);
        return OrderResponseDTO.fromEntity(saved);
    }
}
