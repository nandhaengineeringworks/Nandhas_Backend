package com.company.app.catalogue.service;

import com.company.app.catalogue.entity.MediaAsset;
import com.company.app.catalogue.repository.MediaAssetRepository;
import com.company.app.common.FileStorageService;
import com.company.app.common.PagedResponse;
import com.company.app.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final FileStorageService fileStorageService;
    private final MediaAssetRepository mediaAssetRepository;

    @Transactional
    public MediaAsset uploadMedia(MultipartFile file, String subDir, String uploadedBy) {
        String subDirectory = (subDir != null && !subDir.trim().isEmpty()) ? subDir : "products";
        String fileUrl = fileStorageService.storeFile(file, subDirectory);

        MediaAsset mediaAsset = MediaAsset.builder()
                .fileName(file.getOriginalFilename())
                .url(fileUrl)
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .uploadedBy(uploadedBy != null ? uploadedBy : "admin")
                .build();

        return mediaAssetRepository.save(mediaAsset);
    }

    @Transactional(readOnly = true)
    public PagedResponse<MediaAsset> getMediaAssets(int page, int size) {
        Page<MediaAsset> assetPage = mediaAssetRepository.findAllByOrderByUploadedAtDesc(PageRequest.of(page, size));
        return PagedResponse.from(assetPage);
    }

    @Transactional
    public void deleteMedia(Long id) {
        MediaAsset asset = mediaAssetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MediaAsset", "id", id));
        fileStorageService.deleteFile(asset.getUrl());
        mediaAssetRepository.delete(asset);
    }
}
