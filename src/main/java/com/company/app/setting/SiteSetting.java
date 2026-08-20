package com.company.app.setting;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "site_settings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "setting_key", nullable = false, unique = true)
    private String settingKey;

    @Column(name = "setting_value", length = 2000)
    private String settingValue;

    @Column(length = 500)
    private String description;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
