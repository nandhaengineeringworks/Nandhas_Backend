package com.company.app.banner;

import com.company.app.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/banners")
@RequiredArgsConstructor
@Tag(name = "Admin - Banners", description = "Admin Banner Slider Management APIs")
@SecurityRequirement(name = "BearerAuth")
@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_PRODUCT_MANAGER')")
public class BannerAdminController {

    private final BannerService bannerService;

    @GetMapping
    @Operation(summary = "Get all banners for admin management")
    public ResponseEntity<ApiResponse<List<BannerDTO>>> getAllBanners() {
        List<BannerDTO> list = bannerService.getAllBannersAdmin();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping
    @Operation(summary = "Create promotional banner")
    public ResponseEntity<ApiResponse<BannerDTO>> createBanner(@Valid @RequestBody BannerDTO dto) {
        BannerDTO created = bannerService.createBanner(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Banner created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update promotional banner")
    public ResponseEntity<ApiResponse<BannerDTO>> updateBanner(@PathVariable Long id, @Valid @RequestBody BannerDTO dto) {
        BannerDTO updated = bannerService.updateBanner(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Banner updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete banner")
    public ResponseEntity<ApiResponse<Void>> deleteBanner(@PathVariable Long id) {
        bannerService.deleteBanner(id);
        return ResponseEntity.ok(ApiResponse.success("Banner deleted successfully", null));
    }
}
