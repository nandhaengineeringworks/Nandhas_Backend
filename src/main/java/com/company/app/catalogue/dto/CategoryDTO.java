package com.company.app.catalogue.dto;

import com.company.app.catalogue.entity.Category;
import com.company.app.catalogue.entity.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private CategoryType type;
    private String imageUrl;
    private String bannerUrl;
    private Long parentId;
    private String parentName;
    private Integer sortOrder;
    private Boolean isActive;
    private long productCount;
    @Builder.Default
    private List<CategoryDTO> subCategories = new ArrayList<>();

    public static CategoryDTO fromEntity(Category category) {
        if (category == null) return null;
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .type(category.getType())
                .imageUrl(category.getImageUrl())
                .bannerUrl(category.getBannerUrl())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .sortOrder(category.getSortOrder())
                .isActive(category.getIsActive())
                .productCount(category.getProducts() != null ? category.getProducts().size() : 0)
                .subCategories(category.getSubCategories() != null ?
                        category.getSubCategories().stream()
                                .map(CategoryDTO::fromEntity)
                                .collect(Collectors.toList()) : new ArrayList<>())
                .build();
    }
}
