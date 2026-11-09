package com.example.jobportal.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.jobportal.model.UserAccount;
import com.example.jobportal.model.UserRole;
import com.example.jobportal.repository.UserAccountRepository;

@Component
public class AccountInitializer implements CommandLineRunner {
    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    public AccountInitializer(UserAccountRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (adminEmail.isBlank() || adminPassword.isBlank() || users.existsByEmailIgnoreCase(adminEmail)) return;
        users.save(new UserAccount(adminEmail.trim().toLowerCase(), passwordEncoder.encode(adminPassword),
                "Portal Administrator", "+10000000000", UserRole.ADMIN));
    }
}