package com.company.app.order;

import com.company.app.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Public - Orders", description = "Public checkout & order tracking APIs")
public class OrderPublicController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Place a new order with cart items")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> createOrder(@Valid @RequestBody OrderRequestDTO dto) {
        OrderResponseDTO order = orderService.createOrder(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Order placed successfully", order));
    }

    @GetMapping("/lookup/{orderNumber}")
    @Operation(summary = "Look up order by order number")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> lookupOrder(@PathVariable String orderNumber) {
        OrderResponseDTO order = orderService.getOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.success(order));
    }
}
