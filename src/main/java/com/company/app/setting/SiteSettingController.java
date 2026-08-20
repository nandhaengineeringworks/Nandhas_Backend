package com.company.app.setting;

import com.company.app.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Settings", description = "Global Storefront and B2B Site Settings API")
public class SiteSettingController {

    private final SiteSettingService settingService;

    @GetMapping("/settings/public")
    @Operation(summary = "Get Public Storefront Settings (Price visibility, B2B mode)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPublicSettings() {
        return ResponseEntity.ok(ApiResponse.success(settingService.getPublicSettings()));
    }

    @GetMapping("/admin/settings")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_PRODUCT_MANAGER')")
    @Operation(summary = "Get All Site Settings (Admin only)")
    public ResponseEntity<ApiResponse<List<SiteSetting>>> getAllSettings() {
        return ResponseEntity.ok(ApiResponse.success(settingService.getAllSettings()));
    }

    @PutMapping("/admin/settings")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_PRODUCT_MANAGER')")
    @Operation(summary = "Update Storefront Settings (Admin only)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateSettings(@RequestBody Map<String, Object> updates) {
        Map<String, Object> updated = settingService.updateSettingsMap(updates);
        return ResponseEntity.ok(ApiResponse.success("Storefront settings updated successfully", updated));
    }
}
