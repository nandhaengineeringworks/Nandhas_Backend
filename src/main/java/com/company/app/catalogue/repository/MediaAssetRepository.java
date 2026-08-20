package com.company.app.catalogue.repository;

import com.company.app.catalogue.entity.MediaAsset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {
    Page<MediaAsset> findAllByOrderByUploadedAtDesc(Pageable pageable);
}
