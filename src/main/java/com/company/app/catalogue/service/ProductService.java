package com.company.app.catalogue.service;

import com.company.app.catalogue.dto.*;
import com.company.app.catalogue.entity.*;
import com.company.app.catalogue.repository.CategoryRepository;
import com.company.app.catalogue.repository.ProductImageRepository;
import com.company.app.catalogue.repository.ProductRepository;
import com.company.app.catalogue.repository.ProductSpecRepository;
import com.company.app.catalogue.repository.ProductVariantRepository;
import com.company.app.common.BadRequestException;
import com.company.app.common.PagedResponse;
import com.company.app.common.ResourceNotFoundException;
import com.company.app.common.SlugUtils;
import com.company.app.enquiry.EnquiryRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductSpecRepository productSpecRepository;
    private final ProductVariantRepository productVariantRepository;
    private final EnquiryRepository enquiryRepository;

    // Reject browser-local or temporary URLs — these must never be persisted
    private String sanitizeImageUrl(String url) {
        if (url != null && (url.startsWith("blob:") || url.startsWith("data:") || url.startsWith("file:"))) {
            throw new IllegalArgumentException("Temporary image URLs cannot be saved");
        }
        return url;
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProductSummaryDTO> getProducts(ProductFilterRequest filter) {
        Sort sort = Sort.by(Sort.Direction.ASC, "sortOrder").and(Sort.by(Sort.Direction.DESC, "id"));
        if ("price_asc".equalsIgnoreCase(filter.getSortBy())) {
            sort = Sort.by(Sort.Direction.ASC, "price");
        } else if ("price_desc".equalsIgnoreCase(filter.getSortBy())) {
            sort = Sort.by(Sort.Direction.DESC, "price");
        } else if ("name_asc".equalsIgnoreCase(filter.getSortBy())) {
            sort = Sort.by(Sort.Direction.ASC, "name");
        } else if ("newest".equalsIgnoreCase(filter.getSortBy())) {
            sort = Sort.by(Sort.Direction.DESC, "createdAt");
        }

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("status"), ProductStatus.PUBLISHED));

            if (filter.getType() != null) {
                predicates.add(cb.equal(root.get("category").get("type"), filter.getType()));
            }

            if (filter.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), filter.getCategoryId()));
            }

            if (StringUtils.hasText(filter.getCategorySlug())) {
                predicates.add(cb.equal(root.get("category").get("slug"), filter.getCategorySlug()));
            }

            if (StringUtils.hasText(filter.getSearch())) {
                String likePattern = "%" + filter.getSearch().trim().toLowerCase() + "%";
                Predicate nameLike = cb.like(cb.lower(root.get("name")), likePattern);
                Predicate descLike = cb.like(cb.lower(root.get("shortDesc")), likePattern);
                Predicate skuLike = cb.like(cb.lower(root.get("sku")), likePattern);
                Predicate modelLike = cb.like(cb.lower(root.get("modelNumber")), likePattern);
                predicates.add(cb.or(nameLike, descLike, skuLike, modelLike));
            }

            if (filter.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));
            }

            if (filter.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));
            }

            if (Boolean.TRUE.equals(filter.getIsFeatured())) {
                predicates.add(cb.isTrue(root.get("isFeatured")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Product> page = productRepository.findAll(spec, pageable);
        List<ProductSummaryDTO> dtos = page.getContent().stream()
                .map(ProductSummaryDTO::fromEntity)
                .collect(Collectors.toList());

        return PagedResponse.from(page, dtos);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProductSummaryDTO> getAllProductsAdmin(int page, int size, String search, Long categoryId, ProductStatus status) {
        Sort sort = Sort.by(Sort.Direction.ASC, "sortOrder").and(Sort.by(Sort.Direction.DESC, "id"));
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (categoryId != null) {
                Predicate catMatch = cb.equal(root.get("category").get("id"), categoryId);
                Predicate parentCatMatch = cb.equal(root.get("category").get("parent").get("id"), categoryId);
                predicates.add(cb.or(catMatch, parentCatMatch));
            }

            if (StringUtils.hasText(search)) {
                String likePattern = "%" + search.trim().toLowerCase() + "%";
                Predicate nameLike = cb.like(cb.lower(root.get("name")), likePattern);
                Predicate descLike = cb.like(cb.lower(root.get("shortDesc")), likePattern);
                Predicate skuLike = cb.like(cb.lower(root.get("sku")), likePattern);
                Predicate modelLike = cb.like(cb.lower(root.get("modelNumber")), likePattern);
                predicates.add(cb.or(nameLike, descLike, skuLike, modelLike));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Product> productPage = productRepository.findAll(spec, pageable);
        List<ProductSummaryDTO> dtos = productPage.getContent().stream()
                .map(ProductSummaryDTO::fromEntity)
                .collect(Collectors.toList());

        return PagedResponse.from(productPage, dtos);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProductSummaryDTO> getAdminProducts(ProductFilterRequest filter) {
        Sort sort = Sort.by(Sort.Direction.ASC, "sortOrder").and(Sort.by(Sort.Direction.DESC, "id"));
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getType() != null) {
                predicates.add(cb.equal(root.get("category").get("type"), filter.getType()));
            }

            if (filter.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), filter.getCategoryId()));
            }

            if (StringUtils.hasText(filter.getSearch())) {
                String likePattern = "%" + filter.getSearch().toLowerCase() + "%";
                Predicate nameLike = cb.like(cb.lower(root.get("name")), likePattern);
                Predicate descLike = cb.like(cb.lower(root.get("shortDesc")), likePattern);
                predicates.add(cb.or(nameLike, descLike));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Product> productPage = productRepository.findAll(spec, pageable);
        List<ProductSummaryDTO> dtos = productPage.getContent().stream()
                .map(ProductSummaryDTO::fromEntity)
                .collect(Collectors.toList());

        return PagedResponse.from(productPage, dtos);
    }

    @Transactional(readOnly = true)
    public ProductDetailDTO getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "slug", slug));
        return ProductDetailDTO.fromEntity(product);
    }

    @Transactional(readOnly = true)
    public ProductDetailDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return ProductDetailDTO.fromEntity(product);
    }

    @Transactional(readOnly = true)
    public List<ProductSummaryDTO> getFeaturedProducts(CategoryType type) {
        List<Product> products = type != null ?
                productRepository.findFeaturedByType(type) :
                productRepository.findFeaturedAll();

        return products.stream()
                .map(ProductSummaryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductSummaryDTO> getRelatedProducts(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        List<Product> related = productRepository.findRelatedProducts(
                product.getCategory().getId(), productId, PageRequest.of(0, 4));

        return related.stream()
                .map(ProductSummaryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductDetailDTO createProduct(ProductRequestDTO dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));

        String slug = StringUtils.hasText(dto.getSlug()) ? SlugUtils.toSlug(dto.getSlug()) : SlugUtils.toSlug(dto.getName());
        if (productRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis() % 10000;
        }

        Product product = Product.builder()
                .category(category)
                .name(dto.getName())
                .slug(slug)
                .sku(dto.getSku())
                .modelNumber(dto.getModelNumber())
                .shortDesc(dto.getShortDesc())
                .description(dto.getDescription())
                .productType(dto.getProductType() != null ? dto.getProductType() : "Machinery")
                .brand(dto.getBrand() != null ? dto.getBrand() : "Nandhas")
                .manufacturer(dto.getManufacturer())
                .countryOfOrigin(dto.getCountryOfOrigin() != null ? dto.getCountryOfOrigin() : "India")
                .price(dto.getPrice())
                .compareAtPrice(dto.getCompareAtPrice())
                .startingFromPrice(dto.getStartingFromPrice())
                .priceMode(dto.getPriceMode() != null ? dto.getPriceMode() : "QUOTE_ONLY")
                .gstPercentage(dto.getGstPercentage() != null ? dto.getGstPercentage() : new BigDecimal("18.00"))
                .isQuoteOnly(dto.getIsQuoteOnly() != null ? dto.getIsQuoteOnly() : true)
                .brochureUrl(dto.getBrochureUrl())
                .primaryImageUrl(sanitizeImageUrl(dto.getPrimaryImageUrl()))
                .technicalDrawingUrl(dto.getTechnicalDrawingUrl())
                .videoUrl(dto.getVideoUrl())
                .view360Url(dto.getView360Url())
                .applicationsJson(dto.getApplicationsJson())
                .availability(dto.getAvailability() != null ? dto.getAvailability() : "MADE_TO_ORDER")
                .stockQuantity(dto.getStockQuantity())
                .minOrderQuantity(dto.getMinOrderQuantity() != null ? dto.getMinOrderQuantity() : 1)
                .productionLeadTime(dto.getProductionLeadTime())
                .productWeight(dto.getProductWeight())
                .packageDimensions(dto.getPackageDimensions())
                .deliveryTime(dto.getDeliveryTime())
                .isInstallationAvailable(dto.getIsInstallationAvailable() != null ? dto.getIsInstallationAvailable() : true)
                .installationCharges(dto.getInstallationCharges())
                .warrantyPeriod(dto.getWarrantyPeriod() != null ? dto.getWarrantyPeriod() : "1 Year Comprehensive")
                .serviceLocations(dto.getServiceLocations() != null ? dto.getServiceLocations() : "Pan India")
                .isSparePartsAvailable(dto.getIsSparePartsAvailable() != null ? dto.getIsSparePartsAvailable() : true)
                .isAmcAvailable(dto.getIsAmcAvailable() != null ? dto.getIsAmcAvailable() : true)
                .seoTitle(dto.getSeoTitle())
                .seoDescription(dto.getSeoDescription())
                .seoKeywords(dto.getSeoKeywords())
                .status(dto.getStatus() != null ? dto.getStatus() : ProductStatus.PUBLISHED)
                .isFeatured(dto.getIsFeatured() != null ? dto.getIsFeatured() : false)
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .build();

        Product savedProduct = productRepository.save(product);

        // Images
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            int order = 0;
            for (ProductImageDTO imgDto : dto.getImages()) {
                String imgUrl = sanitizeImageUrl(imgDto.getImageUrl());
                if (imgUrl == null) continue; // skip blob: URLs
                ProductImage image = ProductImage.builder()
                        .product(savedProduct)
                        .imageUrl(imgUrl)
                        .altText(imgDto.getAltText() != null ? imgDto.getAltText() : savedProduct.getName())
                        .sortOrder(imgDto.getSortOrder() != null ? imgDto.getSortOrder() : order++)
                        .isPrimary(Boolean.TRUE.equals(imgDto.getIsPrimary()))
                        .build();
                savedProduct.addImage(image);
            }
            if (savedProduct.getPrimaryImageUrl() == null && !savedProduct.getImages().isEmpty()) {
                savedProduct.setPrimaryImageUrl(savedProduct.getImages().get(0).getImageUrl());
            }
        }

        // Specs
        if (dto.getSpecs() != null && !dto.getSpecs().isEmpty()) {
            int order = 0;
            for (ProductSpecDTO specDto : dto.getSpecs()) {
                ProductSpec spec = ProductSpec.builder()
                        .product(savedProduct)
                        .specKey(specDto.getSpecKey())
                        .specValue(specDto.getSpecValue())
                        .specGroup(specDto.getSpecGroup() != null ? specDto.getSpecGroup() : "General")
                        .sortOrder(specDto.getSortOrder() != null ? specDto.getSortOrder() : order++)
                        .build();
                savedProduct.addSpec(spec);
            }
        }

        // Variants
        if (dto.getVariants() != null && !dto.getVariants().isEmpty()) {
            for (ProductVariantDTO varDto : dto.getVariants()) {
                ProductVariant variant = ProductVariant.builder()
                        .product(savedProduct)
                        .variantName(varDto.getVariantName())
                        .sku(varDto.getSku())
                        .price(varDto.getPrice() != null ? varDto.getPrice() : savedProduct.getPrice())
                        .stockQty(varDto.getStockQty() != null ? varDto.getStockQty() : 10)
                        .imageUrl(varDto.getImageUrl())
                        .isDefault(Boolean.TRUE.equals(varDto.getIsDefault()))
                        .build();
                savedProduct.addVariant(variant);
            }
        }

        Product reSaved = productRepository.save(savedProduct);
        return ProductDetailDTO.fromEntity(reSaved);
    }

    @Transactional
    public ProductDetailDTO updateProduct(Long id, ProductRequestDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (!product.getCategory().getId().equals(dto.getCategoryId())) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));
            product.setCategory(category);
        }

        product.setName(dto.getName());
        if (StringUtils.hasText(dto.getSlug()) && !dto.getSlug().equals(product.getSlug())) {
            String newSlug = SlugUtils.toSlug(dto.getSlug());
            if (productRepository.existsBySlug(newSlug)) {
                throw new BadRequestException("Slug already in use: " + newSlug);
            }
            product.setSlug(newSlug);
        }

        product.setSku(dto.getSku());
        product.setModelNumber(dto.getModelNumber());
        product.setShortDesc(dto.getShortDesc());
        product.setDescription(dto.getDescription());
        if (dto.getProductType() != null) product.setProductType(dto.getProductType());
        if (dto.getBrand() != null) product.setBrand(dto.getBrand());
        product.setManufacturer(dto.getManufacturer());
        if (dto.getCountryOfOrigin() != null) product.setCountryOfOrigin(dto.getCountryOfOrigin());
        product.setPrice(dto.getPrice());
        product.setCompareAtPrice(dto.getCompareAtPrice());
        product.setStartingFromPrice(dto.getStartingFromPrice());
        if (dto.getPriceMode() != null) product.setPriceMode(dto.getPriceMode());
        if (dto.getGstPercentage() != null) product.setGstPercentage(dto.getGstPercentage());
        if (dto.getIsQuoteOnly() != null) product.setIsQuoteOnly(dto.getIsQuoteOnly());
        product.setBrochureUrl(dto.getBrochureUrl());
        product.setPrimaryImageUrl(sanitizeImageUrl(dto.getPrimaryImageUrl()));
        product.setTechnicalDrawingUrl(dto.getTechnicalDrawingUrl());
        product.setVideoUrl(dto.getVideoUrl());
        product.setView360Url(dto.getView360Url());
        product.setApplicationsJson(dto.getApplicationsJson());
        if (dto.getAvailability() != null) product.setAvailability(dto.getAvailability());
        product.setStockQuantity(dto.getStockQuantity());
        if (dto.getMinOrderQuantity() != null) product.setMinOrderQuantity(dto.getMinOrderQuantity());
        product.setProductionLeadTime(dto.getProductionLeadTime());
        product.setProductWeight(dto.getProductWeight());
        product.setPackageDimensions(dto.getPackageDimensions());
        product.setDeliveryTime(dto.getDeliveryTime());
        if (dto.getIsInstallationAvailable() != null) product.setIsInstallationAvailable(dto.getIsInstallationAvailable());
        product.setInstallationCharges(dto.getInstallationCharges());
        if (dto.getWarrantyPeriod() != null) product.setWarrantyPeriod(dto.getWarrantyPeriod());
        if (dto.getServiceLocations() != null) product.setServiceLocations(dto.getServiceLocations());
        if (dto.getIsSparePartsAvailable() != null) product.setIsSparePartsAvailable(dto.getIsSparePartsAvailable());
        if (dto.getIsAmcAvailable() != null) product.setIsAmcAvailable(dto.getIsAmcAvailable());
        product.setSeoTitle(dto.getSeoTitle());
        product.setSeoDescription(dto.getSeoDescription());
        product.setSeoKeywords(dto.getSeoKeywords());

        if (dto.getStatus() != null) product.setStatus(dto.getStatus());
        if (dto.getIsFeatured() != null) product.setIsFeatured(dto.getIsFeatured());
        if (dto.getSortOrder() != null) product.setSortOrder(dto.getSortOrder());

        // Update Images
        if (dto.getImages() != null) {
            product.getImages().clear();
            int order = 0;
            for (ProductImageDTO imgDto : dto.getImages()) {
                String imgUrl = sanitizeImageUrl(imgDto.getImageUrl());
                if (imgUrl == null) continue; // skip blob: URLs
                ProductImage image = ProductImage.builder()
                        .product(product)
                        .imageUrl(imgUrl)
                        .altText(imgDto.getAltText() != null ? imgDto.getAltText() : product.getName())
                        .sortOrder(imgDto.getSortOrder() != null ? imgDto.getSortOrder() : order++)
                        .isPrimary(Boolean.TRUE.equals(imgDto.getIsPrimary()))
                        .build();
                product.addImage(image);
            }
            if (product.getPrimaryImageUrl() == null && !product.getImages().isEmpty()) {
                product.setPrimaryImageUrl(product.getImages().get(0).getImageUrl());
            }
        }

        // Update Specs
        if (dto.getSpecs() != null) {
            product.getSpecs().clear();
            int order = 0;
            for (ProductSpecDTO specDto : dto.getSpecs()) {
                ProductSpec spec = ProductSpec.builder()
                        .product(product)
                        .specKey(specDto.getSpecKey())
                        .specValue(specDto.getSpecValue())
                        .specGroup(specDto.getSpecGroup() != null ? specDto.getSpecGroup() : "General")
                        .sortOrder(specDto.getSortOrder() != null ? specDto.getSortOrder() : order++)
                        .build();
                product.addSpec(spec);
            }
        }

        // Update Variants
        if (dto.getVariants() != null) {
            product.getVariants().clear();
            for (ProductVariantDTO varDto : dto.getVariants()) {
                ProductVariant variant = ProductVariant.builder()
                        .product(product)
                        .variantName(varDto.getVariantName())
                        .sku(varDto.getSku())
                        .price(varDto.getPrice() != null ? varDto.getPrice() : product.getPrice())
                        .stockQty(varDto.getStockQty() != null ? varDto.getStockQty() : 10)
                        .imageUrl(varDto.getImageUrl())
                        .isDefault(Boolean.TRUE.equals(varDto.getIsDefault()))
                        .build();
                product.addVariant(variant);
            }
        }

        Product saved = productRepository.save(product);
        return ProductDetailDTO.fromEntity(saved);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        enquiryRepository.detachProductByProductId(id);
        productRepository.delete(product);
    }
}
