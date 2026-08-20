package com.company.app.banner;

import com.company.app.catalogue.entity.CategoryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "banners")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String subtitle;

    @Column(nullable = false, length = 1000)
    private String imageUrl;

    @Column(length = 1000)
    private String desktopImageUrl;

    @Column(length = 1000)
    private String mobileImageUrl;

    @Column(length = 1000)
    private String tabletImageUrl;

    private String targetUrl;
    private String ctaText;

    private String smallTag;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String featuresJson;

    @Builder.Default
    private String alignment = "LEFT"; // LEFT, CENTER, RIGHT

    @Builder.Default
    private String verticalPosition = "CENTER"; // TOP, CENTER, BOTTOM

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

    @Lob
    @Column(columnDefinition = "TEXT")
    private String mobileSettingsJson;

    @Enumerated(EnumType.STRING)
    private CategoryType type; // MACHINERY, INTERIOR or NULL for general

    @Builder.Default
    private Integer sortOrder = 0;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
