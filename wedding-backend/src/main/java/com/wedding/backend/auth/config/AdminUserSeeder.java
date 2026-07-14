package com.wedding.backend.auth.config;

import com.wedding.backend.auth.model.AdminUser;
import com.wedding.backend.auth.repository.AdminUserRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminUserSeeder {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserSeeder(AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seedDefaultAdmin() {
        if (adminUserRepository.existsByUsernameIgnoreCase("Admin")) {
            return;
        }

        AdminUser admin = new AdminUser();
        admin.setUsername("Admin");
        admin.setPasswordHash(passwordEncoder.encode("01102021"));
        adminUserRepository.save(admin);
    }
}
