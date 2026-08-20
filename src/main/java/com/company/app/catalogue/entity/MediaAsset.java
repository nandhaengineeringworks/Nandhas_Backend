package com.company.app.catalogue.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "media_assets")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false, length = 1000)
    private String url;

    private String fileType; // image/jpeg, application/pdf, etc.

    private Long fileSize;

    private String uploadedBy;

    @CreationTimestamp
    private LocalDateTime uploadedAt;
}
