package com.company.app.firebase;

import com.company.app.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/firebase")
@RequiredArgsConstructor
@Tag(name = "Firebase Phone Authentication")
public class FirebaseAuthController {

    private final FirebaseAuthService firebaseAuthService;

    @PostMapping("/verify")
    @Operation(summary = "Verify a Firebase phone-auth ID token")
    public ResponseEntity<ApiResponse<FirebaseAuthService.VerifiedPhone>> verify(@RequestBody VerifyRequest request) {
        FirebaseAuthService.VerifiedPhone verifiedPhone = firebaseAuthService.verifyIdToken(request.idToken());
        return ResponseEntity.ok(ApiResponse.success("Phone number verified", verifiedPhone));
    }

    public record VerifyRequest(@NotBlank String idToken) {}
}
