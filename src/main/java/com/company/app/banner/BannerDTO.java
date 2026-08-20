package com.company.app.banner;

import com.company.app.catalogue.entity.CategoryType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BannerDTO {
    private Long id;
    private String title;
    private String subtitle;
    private String smallTag;
    private String imageUrl;
    private String desktopImageUrl;
    private String tabletImageUrl;
    private String mobileImageUrl;
    private String targetUrl;
    private String featuresJson;
    
    @Builder.Default
    private String alignment = "LEFT";
    
    @Builder.Default
    private String verticalPosition = "CENTER";
    
    @Builder.Default
    private String contentWidth = "600px";
    
    @Builder.Default
    private Integer overlayOpacity = 60;
    
    @Builder.Default
    private String overlayColor = "#000000";
    
    private String primaryButtonText;
    private String primaryButtonLink;
    
    @Builder.Default
    private Boolean primaryButtonEnabled = true;
    
    private String secondaryButtonText;
    private String secondaryButtonLink;
    
    @Builder.Default
    private Boolean secondaryButtonEnabled = false;
    
    private String xPosition;
    private String yPosition;
    private String mobileSettingsJson;
    private CategoryType type;
    
    @Builder.Default
    private Integer sortOrder = 0;
    
    @Builder.Default
    private Boolean isActive = true;

    public static BannerDTO fromEntity(Banner banner) {
        if (banner == null) return null;
        return BannerDTO.builder()
                .id(banner.getId())
                .title(banner.getTitle())
                .subtitle(banner.getSubtitle())
                .smallTag(banner.getSmallTag())
                .imageUrl(banner.getImageUrl())
                .desktopImageUrl(banner.getDesktopImageUrl() != null ? banner.getDesktopImageUrl() : banner.getImageUrl())
                .tabletImageUrl(banner.getTabletImageUrl() != null ? banner.getTabletImageUrl() : banner.getDesktopImageUrl())
                .mobileImageUrl(banner.getMobileImageUrl() != null ? banner.getMobileImageUrl() : banner.getImageUrl())
                .targetUrl(banner.getTargetUrl())
                .featuresJson(banner.getFeaturesJson())
                .alignment(banner.getAlignment() != null ? banner.getAlignment() : "LEFT")
                .verticalPosition(banner.getVerticalPosition() != null ? banner.getVerticalPosition() : "CENTER")
                .contentWidth(banner.getContentWidth() != null ? banner.getContentWidth() : "600px")
                .overlayOpacity(banner.getOverlayOpacity() != null ? banner.getOverlayOpacity() : 60)
                .overlayColor(banner.getOverlayColor() != null ? banner.getOverlayColor() : "#000000")
                .primaryButtonText(banner.getPrimaryButtonText())
                .primaryButtonLink(banner.getPrimaryButtonLink())
                .primaryButtonEnabled(banner.getPrimaryButtonEnabled() != null ? banner.getPrimaryButtonEnabled() : true)
                .secondaryButtonText(banner.getSecondaryButtonText())
                .secondaryButtonLink(banner.getSecondaryButtonLink())
                .secondaryButtonEnabled(banner.getSecondaryButtonEnabled() != null ? banner.getSecondaryButtonEnabled() : false)
                .xPosition(banner.getXPosition())
                .yPosition(banner.getYPosition())
                .mobileSettingsJson(banner.getMobileSettingsJson())
                .type(banner.getType())
                .sortOrder(banner.getSortOrder() != null ? banner.getSortOrder() : 0)
                .isActive(banner.getIsActive() != null ? banner.getIsActive() : true)
                .build();
    }
}
