package com.company.app.config;

import com.company.app.admin.AdminRole;
import com.company.app.admin.AdminUser;
import com.company.app.admin.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (adminUserRepository.count() == 0) {
            seedAdminUsers();
        }
        // Zero dummy data: Catalogue, categories, orders, banners, and enquiries
        // are only created by the admin through the panel or via live customer storefront interactions.
    }

    private void seedAdminUsers() {
        log.info("Seeding Default Admin Credentials...");
        AdminUser superAdmin = AdminUser.builder()
                .email("admin@company.com")
                .passwordHash(passwordEncoder.encode("admin123"))
                .fullName("Rajesh Sharma (Super Admin)")
                .phone("+91 9876543210")
                .role(AdminRole.ROLE_SUPER_ADMIN)
                .isActive(true)
                .build();

        AdminUser productManager = AdminUser.builder()
                .email("manager@company.com")
                .passwordHash(passwordEncoder.encode("manager123"))
                .fullName("Ananya Verma (Catalogue Head)")
                .phone("+91 9876543211")
                .role(AdminRole.ROLE_PRODUCT_MANAGER)
                .isActive(true)
                .build();

        AdminUser salesLead = AdminUser.builder()
                .email("sales@company.com")
                .passwordHash(passwordEncoder.encode("sales123"))
                .fullName("Vikram Patel (Sales Specialist)")
                .phone("+91 9876543212")
                .role(AdminRole.ROLE_SALES)
                .isActive(true)
                .build();

        adminUserRepository.saveAll(List.of(superAdmin, productManager, salesLead));
    }
}
