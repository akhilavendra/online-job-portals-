package com.example.jobportal.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.jobportal.repository.UserAccountRepository;

@Service
public class PortalUserDetailsService implements UserDetailsService {
    private final UserAccountRepository users;

    public PortalUserDetailsService(UserAccountRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return users.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Account not found"));
    }
}