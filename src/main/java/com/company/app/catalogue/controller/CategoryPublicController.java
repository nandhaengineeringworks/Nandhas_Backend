package com.company.app.catalogue.controller;

import com.company.app.catalogue.dto.CategoryDTO;
import com.company.app.catalogue.entity.CategoryType;
import com.company.app.catalogue.service.CategoryService;
import com.company.app.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Public - Categories", description = "Public category navigation & discovery APIs")
public class CategoryPublicController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get active categories with optional type filter")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getCategories(
            @RequestParam(required = false) CategoryType type,
            @RequestParam(required = false, defaultValue = "false") Boolean rootOnly
    ) {
        List<CategoryDTO> categories = categoryService.getAllCategories(type, rootOnly);
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get category details by slug")
    public ResponseEntity<ApiResponse<CategoryDTO>> getCategoryBySlug(@PathVariable String slug) {
        CategoryDTO category = categoryService.getCategoryBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(category));
    }
}
