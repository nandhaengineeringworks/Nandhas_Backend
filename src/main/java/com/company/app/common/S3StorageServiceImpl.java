package com.company.app.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "s3")
public class S3StorageServiceImpl implements FileStorageService {

    @Value("${app.aws.s3.bucket-name}")
    private String bucketName;

    @Value("${app.aws.s3.region:ap-south-2}")
    private String region;

    @Value("${app.aws.s3.access-key:}")
    private String accessKey;

    @Value("${app.aws.s3.secret-key:}")
    private String secretKey;

    @Value("${app.aws.s3.cloudfront-domain:}")
    private String cloudFrontDomain;

    private S3Client getS3Client() {
        if (StringUtils.hasText(accessKey) && StringUtils.hasText(secretKey)) {
            return S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                    .build();
        } else {
            return S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
        }
    }

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

        String s3Key = subDirectory + "/" + UUID.randomUUID() + extension;

        try (S3Client s3Client = getS3Client()) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            if (StringUtils.hasText(cloudFrontDomain)) {
                return "https://" + cloudFrontDomain + "/" + s3Key;
            } else {
                return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + s3Key;
            }
        } catch (IOException e) {
            throw new BadRequestException("Failed to upload file to S3: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileUrlOrKey) {
        if (!StringUtils.hasText(fileUrlOrKey)) {
            return;
        }
        try (S3Client s3Client = getS3Client()) {
            String key = fileUrlOrKey;
            if (fileUrlOrKey.contains(".amazonaws.com/")) {
                key = fileUrlOrKey.substring(fileUrlOrKey.indexOf(".amazonaws.com/") + 15);
            } else if (StringUtils.hasText(cloudFrontDomain) && fileUrlOrKey.contains(cloudFrontDomain)) {
                key = fileUrlOrKey.substring(fileUrlOrKey.indexOf(cloudFrontDomain) + cloudFrontDomain.length() + 1);
            }
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            // Log error
        }
    }
}
