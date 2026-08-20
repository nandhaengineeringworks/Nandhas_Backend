package com.company.app.catalogue.controller;

import com.company.app.catalogue.dto.ProductDetailDTO;
import com.company.app.catalogue.dto.ProductFilterRequest;
import com.company.app.catalogue.dto.ProductSummaryDTO;
import com.company.app.catalogue.entity.CategoryType;
import com.company.app.catalogue.service.ProductService;
import com.company.app.common.ApiResponse;
import com.company.app.common.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Public - Products", description = "Public product catalogue, search, filters & details")
public class ProductPublicController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Filter & browse products with pagination")
    public ResponseEntity<ApiResponse<PagedResponse<ProductSummaryDTO>>> getProducts(
            @RequestParam(required = false) CategoryType type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String categorySlug,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean isFeatured,
            @RequestParam(required = false, defaultValue = "sortOrder") String sortBy,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "12") int size
    ) {
        ProductFilterRequest filter = ProductFilterRequest.builder()
                .type(type)
                .categoryId(categoryId)
                .categorySlug(categorySlug)
                .search(search)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .isFeatured(isFeatured)
                .sortBy(sortBy)
                .page(page)
                .size(size)
                .build();

        PagedResponse<ProductSummaryDTO> response = productService.getProducts(filter);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get full product details by slug")
    public ResponseEntity<ApiResponse<ProductDetailDTO>> getProductBySlug(@PathVariable String slug) {
        ProductDetailDTO product = productService.getProductBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured products for homepage showcase")
    public ResponseEntity<ApiResponse<List<ProductSummaryDTO>>> getFeaturedProducts(
            @RequestParam(required = false) CategoryType type
    ) {
        List<ProductSummaryDTO> featured = productService.getFeaturedProducts(type);
        return ResponseEntity.ok(ApiResponse.success(featured));
    }

    @GetMapping("/{id}/related")
    @Operation(summary = "Get related products in the same category")
    public ResponseEntity<ApiResponse<List<ProductSummaryDTO>>> getRelatedProducts(@PathVariable Long id) {
        List<ProductSummaryDTO> related = productService.getRelatedProducts(id);
        return ResponseEntity.ok(ApiResponse.success(related));
    }
}
