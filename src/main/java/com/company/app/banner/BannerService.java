package com.company.app.banner;

import com.company.app.catalogue.entity.CategoryType;
import com.company.app.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BannerService {
    private final BannerRepository bannerRepository;

    @Transactional(readOnly = true)
    public List<BannerDTO> getActiveBanners(CategoryType type) {
        List<Banner> banners = type != null 
                ? bannerRepository.findByTypeAndIsActiveTrueOrderBySortOrderAsc(type) 
                : bannerRepository.findByIsActiveTrueOrderBySortOrderAsc();
        return banners.stream().map(BannerDTO::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BannerDTO> getAllBannersAdmin() {
        return bannerRepository.findAll(Sort.by(Sort.Direction.ASC, "sortOrder"))
                .stream().map(BannerDTO::fromEntity).collect(Collectors.toList());
    }

    @Transactional
    public BannerDTO createBanner(BannerDTO dto) {
        String primaryImg = dto.getDesktopImageUrl() != null ? dto.getDesktopImageUrl() : dto.getImageUrl();
        Banner banner = Banner.builder()
                .title(dto.getTitle() != null ? dto.getTitle() : "NANDHAS Machinery & Interiors")
                .subtitle(dto.getSubtitle())
                .smallTag(dto.getSmallTag())
                .imageUrl(primaryImg)
                .desktopImageUrl(primaryImg)
                .tabletImageUrl(dto.getTabletImageUrl() != null ? dto.getTabletImageUrl() : primaryImg)
                .mobileImageUrl(dto.getMobileImageUrl() != null ? dto.getMobileImageUrl() : primaryImg)
                .targetUrl(dto.getTargetUrl())
                .featuresJson(dto.getFeaturesJson())
                .alignment(dto.getAlignment() != null ? dto.getAlignment() : "LEFT")
                .verticalPosition(dto.getVerticalPosition() != null ? dto.getVerticalPosition() : "CENTER")
                .contentWidth(dto.getContentWidth() != null ? dto.getContentWidth() : "600px")
                .overlayOpacity(dto.getOverlayOpacity() != null ? dto.getOverlayOpacity() : 60)
                .overlayColor(dto.getOverlayColor() != null ? dto.getOverlayColor() : "#000000")
                .primaryButtonText(dto.getPrimaryButtonText())
                .primaryButtonLink(dto.getPrimaryButtonLink())
                .primaryButtonEnabled(dto.getPrimaryButtonEnabled() != null ? dto.getPrimaryButtonEnabled() : true)
                .secondaryButtonText(dto.getSecondaryButtonText())
                .secondaryButtonLink(dto.getSecondaryButtonLink())
                .secondaryButtonEnabled(dto.getSecondaryButtonEnabled() != null ? dto.getSecondaryButtonEnabled() : false)
                .xPosition(dto.getXPosition())
                .yPosition(dto.getYPosition())
                .mobileSettingsJson(dto.getMobileSettingsJson())
                .type(dto.getType())
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .isActive(dto.getIsActive() == null || dto.getIsActive())
                .build();
        return BannerDTO.fromEntity(bannerRepository.save(banner));
    }

    @Transactional
    public BannerDTO updateBanner(Long id, BannerDTO dto) {
        Banner banner = bannerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Banner", "id", id));
        
        if (dto.getTitle() != null) banner.setTitle(dto.getTitle());
        banner.setSubtitle(dto.getSubtitle());
        banner.setSmallTag(dto.getSmallTag());
        
        String primaryImg = dto.getDesktopImageUrl() != null ? dto.getDesktopImageUrl() : dto.getImageUrl();
        if (primaryImg != null) {
            banner.setImageUrl(primaryImg);
            banner.setDesktopImageUrl(primaryImg);
        }
        if (dto.getTabletImageUrl() != null) banner.setTabletImageUrl(dto.getTabletImageUrl());
        if (dto.getMobileImageUrl() != null) banner.setMobileImageUrl(dto.getMobileImageUrl());
        if (dto.getTargetUrl() != null) banner.setTargetUrl(dto.getTargetUrl());
        
        banner.setFeaturesJson(dto.getFeaturesJson());
        if (dto.getAlignment() != null) banner.setAlignment(dto.getAlignment());
        if (dto.getVerticalPosition() != null) banner.setVerticalPosition(dto.getVerticalPosition());
        if (dto.getContentWidth() != null) banner.setContentWidth(dto.getContentWidth());
        if (dto.getOverlayOpacity() != null) banner.setOverlayOpacity(dto.getOverlayOpacity());
        if (dto.getOverlayColor() != null) banner.setOverlayColor(dto.getOverlayColor());
        
        banner.setPrimaryButtonText(dto.getPrimaryButtonText());
        banner.setPrimaryButtonLink(dto.getPrimaryButtonLink());
        if (dto.getPrimaryButtonEnabled() != null) banner.setPrimaryButtonEnabled(dto.getPrimaryButtonEnabled());
        
        banner.setSecondaryButtonText(dto.getSecondaryButtonText());
        banner.setSecondaryButtonLink(dto.getSecondaryButtonLink());
        if (dto.getSecondaryButtonEnabled() != null) banner.setSecondaryButtonEnabled(dto.getSecondaryButtonEnabled());
        
        banner.setXPosition(dto.getXPosition());
        banner.setYPosition(dto.getYPosition());
        banner.setMobileSettingsJson(dto.getMobileSettingsJson());
        banner.setType(dto.getType());
        if (dto.getSortOrder() != null) banner.setSortOrder(dto.getSortOrder());
        if (dto.getIsActive() != null) banner.setIsActive(dto.getIsActive());
        
        return BannerDTO.fromEntity(bannerRepository.save(banner));
    }

    @Transactional
    public void deleteBanner(Long id) {
        Banner banner = bannerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Banner", "id", id));
        bannerRepository.delete(banner);
    }
}
