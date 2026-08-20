package com.company.app.admin;

import com.company.app.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin - Dashboard", description = "Admin Dashboard KPI & Metrics APIs")
@SecurityRequirement(name = "BearerAuth")
@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_PRODUCT_MANAGER', 'ROLE_SALES')")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    @GetMapping("/stats")
    @Operation(summary = "Get high-level dashboard metrics, lead counts, and revenue")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getDashboardStats() {
        DashboardStatsDTO stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
