package com.company.app.catalogue.service;

import com.company.app.catalogue.dto.CategoryDTO;
import com.company.app.catalogue.entity.Category;
import com.company.app.catalogue.entity.CategoryType;
import com.company.app.catalogue.repository.CategoryRepository;
import com.company.app.common.BadRequestException;
import com.company.app.common.ResourceNotFoundException;
import com.company.app.common.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories(CategoryType type, Boolean rootOnly) {
        List<Category> categories;
        if (Boolean.TRUE.equals(rootOnly)) {
            if (type != null) {
                categories = categoryRepository.findByParentIsNullAndTypeAndIsActiveTrueOrderBySortOrderAsc(type);
            } else {
                categories = categoryRepository.findByParentIsNullAndIsActiveTrueOrderBySortOrderAsc();
            }
        } else {
            if (type != null) {
                categories = categoryRepository.findByTypeAndIsActiveTrueOrderBySortOrderAsc(type);
            } else {
                categories = categoryRepository.findAll();
            }
        }

        return categories.stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryDTO getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "slug", slug));
        return CategoryDTO.fromEntity(category);
    }

    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return CategoryDTO.fromEntity(category);
    }

    @Transactional
    public CategoryDTO createCategory(CategoryDTO dto) {
        String slug = StringUtils.hasText(dto.getSlug()) ? SlugUtils.toSlug(dto.getSlug()) : SlugUtils.toSlug(dto.getName());
        if (categoryRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis() % 10000;
        }

        Category parent = null;
        if (dto.getParentId() != null) {
            parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Category", "id", dto.getParentId()));
        }

        Category category = Category.builder()
                .name(dto.getName())
                .slug(slug)
                .description(dto.getDescription())
                .type(dto.getType() != null ? dto.getType() : (parent != null ? parent.getType() : CategoryType.MACHINERY))
                .imageUrl(dto.getImageUrl())
                .bannerUrl(dto.getBannerUrl())
                .parent(parent)
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();

        Category saved = categoryRepository.save(category);
        return CategoryDTO.fromEntity(saved);
    }

    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        category.setName(dto.getName());
        if (StringUtils.hasText(dto.getSlug()) && !dto.getSlug().equals(category.getSlug())) {
            String newSlug = SlugUtils.toSlug(dto.getSlug());
            if (categoryRepository.existsBySlug(newSlug)) {
                throw new BadRequestException("Slug already in use: " + newSlug);
            }
            category.setSlug(newSlug);
        }
        category.setDescription(dto.getDescription());
        if (dto.getType() != null) {
            category.setType(dto.getType());
        }
        category.setImageUrl(dto.getImageUrl());
        category.setBannerUrl(dto.getBannerUrl());
        if (dto.getSortOrder() != null) {
            category.setSortOrder(dto.getSortOrder());
        }
        if (dto.getIsActive() != null) {
            category.setIsActive(dto.getIsActive());
        }

        if (dto.getParentId() != null) {
            if (dto.getParentId().equals(id)) {
                throw new BadRequestException("A category cannot be its own parent");
            }
            Category parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Category", "id", dto.getParentId()));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        Category updated = categoryRepository.save(category);
        return CategoryDTO.fromEntity(updated);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        categoryRepository.delete(category);
    }
}
