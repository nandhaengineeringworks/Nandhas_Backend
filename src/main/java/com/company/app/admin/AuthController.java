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

    @GetMapping("/check-phone")
    @Operation(summary = "Check if customer mobile number exists in database")
    public ResponseEntity<ApiResponse<PhoneCheckResponseDTO>> checkPhone(@RequestParam String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Phone number is required"));
        }

        String raw = phone.trim();
        String formatted = raw;
        if (!formatted.startsWith("+") && formatted.length() == 10) {
            formatted = "+91" + formatted;
        }

        java.util.Optional<Customer> customerOpt = customerRepository.findByPhone(formatted);
        if (customerOpt.isEmpty() && formatted.startsWith("+91")) {
            customerOpt = customerRepository.findByPhone(formatted.substring(3));
        }
        if (customerOpt.isEmpty()) {
            customerOpt = customerRepository.findByPhone(raw);
        }

        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            PhoneCheckResponseDTO dto = PhoneCheckResponseDTO.builder()
                    .exists(true)
                    .fullName(customer.getFullName())
                    .email(customer.getEmail())
                    .build();
            return ResponseEntity.ok(ApiResponse.success("Account found", dto));
        }

        PhoneCheckResponseDTO dto = PhoneCheckResponseDTO.builder()
                .exists(false)
                .fullName(null)
                .email(null)
                .build();
        return ResponseEntity.ok(ApiResponse.success("Account not found", dto));
    }

    @PostMapping("/firebase")
    @Operation(summary = "Authenticate customer using Firebase ID Token")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> authenticateFirebase(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody(required = false) java.util.Map<String, String> payload) {
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

            // Extract optional fullName and email from the request body
            String fullName = "Customer";
            if (payload != null && payload.containsKey("fullName") && payload.get("fullName") != null && !payload.get("fullName").trim().isEmpty()) {
                fullName = payload.get("fullName").trim();
            }

            String email = null;
            if (payload != null && payload.containsKey("email") && payload.get("email") != null && !payload.get("email").trim().isEmpty()) {
                email = payload.get("email").trim().toLowerCase();
            }

            final String finalFullName = fullName;

            // Find customer by Firebase UID or Phone
            java.util.Optional<Customer> existingCustomerOpt = customerRepository.findByFirebaseUid(uid);
            if (existingCustomerOpt.isEmpty()) {
                existingCustomerOpt = customerRepository.findByPhone(phone);
            }
            
            Customer customer;
            if (existingCustomerOpt.isPresent()) {
                customer = existingCustomerOpt.get();
                customer.setFirebaseUid(uid);
                boolean changed = false;
                
                if (finalFullName != null && !finalFullName.equals("Customer") && !finalFullName.equals(customer.getFullName())) {
                    customer.setFullName(finalFullName);
                    changed = true;
                }
                if (email != null && !email.equals(customer.getEmail())) {
                    customer.setEmail(email);
                    changed = true;
                }
                if (changed) {
                    customer = customerRepository.save(customer);
                }
            } else {
                // Create new customer
                Customer newCustomer = Customer.builder()
                        .firebaseUid(uid)
                        .phone(phone)
                        .fullName(finalFullName)
                        .email(email)
                        .build();
                customer = customerRepository.save(newCustomer);
            }

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
                    .email(customer.getEmail() != null ? customer.getEmail() : customer.getPhone())
                    .fullName(customer.getFullName())
                    .role("ROLE_CUSTOMER")
                    .build();

            return ResponseEntity.ok(ApiResponse.success("Firebase login successful", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Firebase authentication failed: " + e.getMessage()));
        }
    }
}
