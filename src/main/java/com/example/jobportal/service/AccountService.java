package com.example.jobportal.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.jobportal.model.UserAccount;
import com.example.jobportal.model.UserRole;
import com.example.jobportal.repository.UserAccountRepository;
import com.example.jobportal.web.form.ProfileForm;
import com.example.jobportal.web.form.RegistrationForm;

@Service
public class AccountService {
    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;

    public AccountService(UserAccountRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserAccount register(RegistrationForm form) {
        String email = form.getEmail().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) throw new IllegalArgumentException("An account with that email already exists.");
        return users.save(new UserAccount(email, passwordEncoder.encode(form.getPassword()), form.getFullName().trim(),
                form.getMobile().trim(), UserRole.JOB_SEEKER));
    }

    @Transactional
    public void updateProfile(UserAccount account, ProfileForm form) {
        account.setFullName(form.getFullName().trim());
        account.setMobile(form.getMobile().trim());
        account.setHeadline(clean(form.getHeadline()));
        account.setLocation(clean(form.getLocation()));
        account.setSkills(clean(form.getSkills()));
        account.setQualification(clean(form.getQualification()));
        account.setInstitution(clean(form.getInstitution()));
        account.setGraduationYear(form.getGraduationYear());
        users.save(account);
    }

    public ProfileForm toProfileForm(UserAccount account) {
        ProfileForm form = new ProfileForm();
        form.setFullName(account.getFullName());
        form.setMobile(account.getMobile());
        form.setHeadline(account.getHeadline());
        form.setLocation(account.getLocation());
        form.setSkills(account.getSkills());
        form.setQualification(account.getQualification());
        form.setInstitution(account.getInstitution());
        form.setGraduationYear(account.getGraduationYear());
        return form;
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}