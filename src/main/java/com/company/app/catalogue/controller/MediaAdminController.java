package com.company.app.catalogue.controller;

import com.company.app.catalogue.entity.MediaAsset;
import com.company.app.catalogue.service.MediaService;
import com.company.app.common.ApiResponse;
import com.company.app.common.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/media")
@RequiredArgsConstructor
@Tag(name = "Admin - Media", description = "Media file / brochure / image upload APIs")
@SecurityRequirement(name = "BearerAuth")
@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_PRODUCT_MANAGER', 'ROLE_SALES')")
public class MediaAdminController {

    private final MediaService mediaService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload image or PDF brochure (Local or S3)")
    public ResponseEntity<ApiResponse<MediaAsset>> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "subDir", required = false, defaultValue = "products") String subDir,
            Authentication authentication
    ) {
        String user = authentication != null ? authentication.getName() : "admin";
        MediaAsset asset = mediaService.uploadMedia(file, subDir, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("File uploaded successfully", asset));
    }

    @GetMapping
    @Operation(summary = "List uploaded media files with pagination")
    public ResponseEntity<ApiResponse<PagedResponse<MediaAsset>>> getMediaAssets(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size
    ) {
        PagedResponse<MediaAsset> list = mediaService.getMediaAssets(page, size);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete uploaded media file")
    public ResponseEntity<ApiResponse<Void>> deleteMedia(@PathVariable Long id) {
        mediaService.deleteMedia(id);
        return ResponseEntity.ok(ApiResponse.success("File deleted successfully", null));
    }
}
