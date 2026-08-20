package com.company.app.common;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String storeFile(MultipartFile file, String subDirectory);
    void deleteFile(String fileUrlOrKey);
}
