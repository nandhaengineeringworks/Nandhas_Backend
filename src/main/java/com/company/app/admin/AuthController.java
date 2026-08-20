package com.company.app.admin;

import com.company.app.common.ApiResponse;
import com.company.app.security.CustomUserDetails;
import com.company.app.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Admin & User Login & Authentication APIs")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final AdminUserRepository adminUserRepository;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user/admin and generate JWT token")
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
                .role(adminUser != null ? adminUser.getRole() : AdminRole.ROLE_SUPER_ADMIN)
                .build();

        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }
}
