package com.company.app.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageServiceImpl implements FileStorageService {

    @Value("${app.storage.local-dir:./uploads}")
    private String uploadDir;

    @Value("${app.storage.base-url:http://localhost:8080/uploads}")
    private String baseUrl;

    @Override
    public String storeFile(MultipartFile file, String subDirectory) {
        if (file.isEmpty()) {
            throw new BadRequestException("Failed to store empty file");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex);
        }

        String storedFileName = UUID.randomUUID().toString() + extension;
        Path targetDir = Paths.get(uploadDir, subDirectory).toAbsolutePath().normalize();

        try {
            Files.createDirectories(targetDir);
            Path targetLocation = targetDir.resolve(storedFileName);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }
            return baseUrl + "/" + subDirectory + "/" + storedFileName;
        } catch (IOException ex) {
            throw new BadRequestException("Could not store file: " + ex.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileUrlOrKey) {
        if (fileUrlOrKey == null || !fileUrlOrKey.contains(baseUrl)) {
            return;
        }
        try {
            String relativePath = fileUrlOrKey.replace(baseUrl + "/", "");
            Path filePath = Paths.get(uploadDir).resolve(relativePath).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Silently ignore deletion error in local dev
        }
    }
}
