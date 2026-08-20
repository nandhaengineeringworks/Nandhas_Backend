package com.company.app.admin;

import com.company.app.common.BadRequestException;
import com.company.app.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<AdminUserDTO> getAllUsers() {
        return adminUserRepository.findAll().stream()
                .map(AdminUserDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public AdminUserDTO createUser(AdminCreateDTO dto) {
        if (adminUserRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email already exists: " + dto.getEmail());
        }

        AdminUser user = AdminUser.builder()
                .email(dto.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .fullName(dto.getFullName())
                .phone(dto.getPhone())
                .role(dto.getRole())
                .isActive(true)
                .build();

        AdminUser saved = adminUserRepository.save(user);
        return AdminUserDTO.fromEntity(saved);
    }

    @Transactional
    public AdminUserDTO updateUserStatus(Long id, Boolean isActive, AdminRole role) {
        AdminUser user = adminUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AdminUser", "id", id));

        if (isActive != null) user.setIsActive(isActive);
        if (role != null) user.setRole(role);

        AdminUser saved = adminUserRepository.save(user);
        return AdminUserDTO.fromEntity(saved);
    }
}
