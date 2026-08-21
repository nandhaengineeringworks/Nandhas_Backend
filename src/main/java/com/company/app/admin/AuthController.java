package com.company.app.admin;

import com.company.app.common.ApiResponse;
import com.company.app.security.CustomUserDetails;
import com.company.app.security.JwtTokenProvider;
import com.company.app.security.Customer;
import com.company.app.security.CustomerRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Admin & User Login & Authentication APIs")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final AdminUserRepository adminUserRepository;
    private final CustomerRepository customerRepository;

    @PostMapping("/login")
    @Operation(summary = "Authenticate admin and generate JWT token")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> authenticateUser(@Valid @RequestBody AuthRequestDTO loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail().toLowerCase().trim(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        AdminUser adminUser = adminUserRepository.findById(userDetails.getId()).orElse(null);

        AuthResponseDTO response = AuthResponseDTO.builder()
                .token(jwt)
                .tokenType("Bearer")
                .id(userDetails.getId())
                .email(userDetails.getUsername())
                .fullName(userDetails.getFullName())
                .role(adminUser != null ? adminUser.getRole().name() : AdminRole.ROLE_SUPER_ADMIN.name())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/firebase")
    @Operation(summary = "Authenticate customer using Firebase ID Token")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> authenticateFirebase(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Missing or invalid Authorization header"));
            }

            String idToken = authorizationHeader.substring(7);
            
            // Verify token with Firebase Admin SDK
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String uid = decodedToken.getUid();
            String phone = decodedToken.getClaims().get("phone_number") != null ? 
                           decodedToken.getClaims().get("phone_number").toString() : null;

            if (phone == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("Phone number missing from Firebase token"));
            }

            // Find or create customer
            Customer customer = customerRepository.findByFirebaseUid(uid).orElseGet(() -> {
                Customer newCustomer = Customer.builder()
                        .firebaseUid(uid)
                        .phone(phone)
                        .fullName("Customer") // Default name, should be updated via profile
                        .build();
                return customerRepository.save(newCustomer);
            });

            // Create Authentication object for Spring Security
            CustomUserDetails userDetails = CustomUserDetails.create(customer);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);

            AuthResponseDTO response = AuthResponseDTO.builder()
                    .token(jwt)
                    .tokenType("Bearer")
                    .id(customer.getId())
                    .email(customer.getPhone()) // For customers, we use phone as the unique identifier
                    .fullName(customer.getFullName())
                    .role("ROLE_CUSTOMER")
                    .build();

            return ResponseEntity.ok(ApiResponse.success("Firebase login successful", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Firebase authentication failed: " + e.getMessage()));
        }
    }
}
