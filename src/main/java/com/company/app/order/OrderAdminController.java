package com.company.app.order;

import com.company.app.common.ApiResponse;
import com.company.app.common.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@Tag(name = "Admin - Orders", description = "Admin Order Management APIs")
@SecurityRequirement(name = "BearerAuth")
@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_PRODUCT_MANAGER', 'ROLE_SALES')")
public class OrderAdminController {

    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "Get paginated orders for admin with optional status filter")
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponseDTO>>> getOrders(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false) OrderStatus status
    ) {
        PagedResponse<OrderResponseDTO> response = orderService.getOrders(page, size, status);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order processing or payment status")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> updateStatus(
            @PathVariable Long id,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) PaymentStatus paymentStatus
    ) {
        OrderResponseDTO order = orderService.updateOrderStatus(id, status, paymentStatus);
        return ResponseEntity.ok(ApiResponse.success("Order status updated", order));
    }
}
