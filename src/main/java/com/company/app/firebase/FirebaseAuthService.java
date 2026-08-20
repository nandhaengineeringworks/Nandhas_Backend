package com.company.app.firebase;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
public class FirebaseAuthService {

    @Value("${app.firebase.enabled:false}")
    private boolean enabled;

    private FirebaseAuth firebaseAuth;

    @PostConstruct
    void initialize() {
        if (!enabled) {
            log.info("Firebase phone authentication is disabled. Set FIREBASE_ENABLED=true to enable it.");
            return;
        }

        try {
            FirebaseApp app = FirebaseApp.getApps().stream().findFirst().orElseGet(() -> {
                try {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.getApplicationDefault())
                            .build();
                    return FirebaseApp.initializeApp(options);
                } catch (IOException exception) {
                    throw new IllegalStateException("Unable to load Firebase credentials", exception);
                }
            });
            firebaseAuth = FirebaseAuth.getInstance(app);
            log.info("Firebase Admin SDK initialized for phone token verification");
        } catch (RuntimeException exception) {
            log.error("Firebase Admin SDK could not be initialized. Check GOOGLE_APPLICATION_CREDENTIALS or AWS secret configuration.", exception);
        }
    }

    public VerifiedPhone verifyIdToken(String idToken) {
        if (!enabled || firebaseAuth == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Firebase phone verification is not configured");
        }
        if (idToken == null || idToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Firebase ID token is required");
        }

        try {
            FirebaseToken token = firebaseAuth.verifyIdToken(idToken, true);
            Map<String, Object> claims = token.getClaims();
            String phoneNumber = claims.get("phone_number") instanceof String value ? value : null;
            if (phoneNumber == null || phoneNumber.isBlank()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The Firebase account is not phone verified");
            }
            return new VerifiedPhone(token.getUid(), phoneNumber);
        } catch (FirebaseAuthException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired Firebase ID token");
        }
    }

    public record VerifiedPhone(String uid, String phoneNumber) {}
}
