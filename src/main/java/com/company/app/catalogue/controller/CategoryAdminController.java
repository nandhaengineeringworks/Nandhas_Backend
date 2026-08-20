package com.company.app.catalogue.controller;

import com.company.app.catalogue.dto.CategoryDTO;
import com.company.app.catalogue.entity.CategoryType;
import com.company.app.catalogue.service.CategoryService;
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
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@Tag(name = "Admin - Categories", description = "Admin Category Management APIs")
@SecurityRequirement(name = "BearerAuth")
@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_PRODUCT_MANAGER')")
public class CategoryAdminController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all categories for admin management")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAllCategories(
            @RequestParam(required = false) CategoryType type
    ) {
        List<CategoryDTO> list = categoryService.getAllCategories(type, false);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<ApiResponse<CategoryDTO>> getCategoryById(@PathVariable Long id) {
        CategoryDTO category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(category));
    }

    @PostMapping
    @Operation(summary = "Create a new category or subcategory")
    public ResponseEntity<ApiResponse<CategoryDTO>> createCategory(@Valid @RequestBody CategoryDTO dto) {
        CategoryDTO created = categoryService.createCategory(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Category created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing category")
    public ResponseEntity<ApiResponse<CategoryDTO>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDTO dto
    ) {
        CategoryDTO updated = categoryService.updateCategory(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully", null));
    }
}
