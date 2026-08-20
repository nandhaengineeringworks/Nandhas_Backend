package com.company.app.banner;

import com.company.app.catalogue.entity.CategoryType;
import com.company.app.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
@Tag(name = "Public - Banners", description = "Public homepage & category promotional sliders")
public class BannerPublicController {

    private final BannerService bannerService;

    @GetMapping
    @Operation(summary = "Get active banners with optional category filter")
    public ResponseEntity<ApiResponse<List<BannerDTO>>> getBanners(
            @RequestParam(required = false) CategoryType type
    ) {
        List<BannerDTO> banners = bannerService.getActiveBanners(type);
        return ResponseEntity.ok(ApiResponse.success(banners));
    }
}
