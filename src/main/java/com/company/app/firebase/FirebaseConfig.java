package com.company.app.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${app.firebase.credentials-json:}")
    private String firebaseCredentialsJson;

    @Value("${app.firebase.credentials-file:}")
    private String firebaseCredentialsFile;

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream credentialsStream = null;

                if (firebaseCredentialsJson != null && !firebaseCredentialsJson.trim().isEmpty()) {
                    credentialsStream = new ByteArrayInputStream(firebaseCredentialsJson.getBytes());
                } else if (firebaseCredentialsFile != null && !firebaseCredentialsFile.trim().isEmpty()) {
                    credentialsStream = new FileInputStream(firebaseCredentialsFile);
                } else {
                    logger.warn("Firebase credentials not found. Firebase Admin SDK will NOT be initialized.");
                    return;
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                        .build();

                FirebaseApp.initializeApp(options);
                logger.info("Firebase Admin SDK initialized successfully.");
            }
        } catch (IOException e) {
            logger.error("Failed to initialize Firebase Admin SDK", e);
        }
    }
}
