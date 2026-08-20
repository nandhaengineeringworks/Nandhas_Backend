package com.company.app.catalogue.controller;

import com.company.app.catalogue.dto.ProductDetailDTO;
import com.company.app.catalogue.dto.ProductRequestDTO;
import com.company.app.catalogue.dto.ProductSummaryDTO;
import com.company.app.catalogue.entity.ProductStatus;
import com.company.app.catalogue.service.ProductService;
import com.company.app.common.ApiResponse;
import com.company.app.common.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@Tag(name = "Admin - Products", description = "Admin Product Management APIs")
@SecurityRequirement(name = "BearerAuth")
@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_PRODUCT_MANAGER')")
public class ProductAdminController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Get paginated products for admin table with search & filter")
    public ResponseEntity<ApiResponse<PagedResponse<ProductSummaryDTO>>> getProducts(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) ProductStatus status
    ) {
        PagedResponse<ProductSummaryDTO> response = productService.getAllProductsAdmin(page, size, search, categoryId, status);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get full product details by ID for editing")
    public ResponseEntity<ApiResponse<ProductDetailDTO>> getProductById(@PathVariable Long id) {
        ProductDetailDTO product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @PostMapping
    @Operation(summary = "Create product with images, dynamic specs & variants")
    public ResponseEntity<ApiResponse<ProductDetailDTO>> createProduct(@Valid @RequestBody ProductRequestDTO dto) {
        ProductDetailDTO created = productService.createProduct(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Product created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product details, specs, images & variants")
    public ResponseEntity<ApiResponse<ProductDetailDTO>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDTO dto
    ) {
        ProductDetailDTO updated = productService.updateProduct(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", null));
    }
}
