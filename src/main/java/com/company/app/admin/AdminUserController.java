package com.company.app.admin;

import com.company.app.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin - Staff & Roles", description = "Admin Staff User & Role Management APIs")
@SecurityRequirement(name = "BearerAuth")
@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @Operation(summary = "List all admin staff members")
    public ResponseEntity<ApiResponse<List<AdminUserDTO>>> getAllUsers() {
        List<AdminUserDTO> users = adminUserService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PostMapping
    @Operation(summary = "Create a new admin staff member")
    public ResponseEntity<ApiResponse<AdminUserDTO>> createUser(@Valid @RequestBody AdminCreateDTO dto) {
        AdminUserDTO created = adminUserService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Admin user created", created));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update user active status and assigned role")
    public ResponseEntity<ApiResponse<AdminUserDTO>> updateUser(
            @PathVariable Long id,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) AdminRole role
    ) {
        AdminUserDTO updated = adminUserService.updateUserStatus(id, isActive, role);
        return ResponseEntity.ok(ApiResponse.success("Admin user updated", updated));
    }
}
