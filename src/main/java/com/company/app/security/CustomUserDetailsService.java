package com.company.app.security;

import com.company.app.admin.AdminUser;
import com.company.app.admin.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AdminUserRepository adminUserRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Try to find customer by phone first
        Optional<Customer> customerOpt = customerRepository.findByPhone(username);
        if (customerOpt.isPresent()) {
            return CustomUserDetails.create(customerOpt.get());
        }

        // Try to find admin by email
        AdminUser adminUser = adminUserRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return CustomUserDetails.create(adminUser);
    }
}
