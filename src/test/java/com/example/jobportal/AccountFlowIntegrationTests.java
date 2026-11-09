package com.example.jobportal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.example.jobportal.repository.UserAccountRepository;

@SpringBootTest
@AutoConfigureMockMvc
class AccountFlowIntegrationTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private UserAccountRepository users;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void registrationValidatesAndLoginUsesEmail() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                        .param("fullName", "Alex Morgan")
                        .param("email", "alex@example.com")
                        .param("mobile", "bad-phone")
                        .param("password", "a-secure-password"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("registrationForm", "mobile"))
                .andExpect(view().name("register"));

        mockMvc.perform(post("/register").with(csrf())
                        .param("fullName", "Alex Morgan")
                        .param("email", "alex@example.com")
                        .param("mobile", "+14155552671")
                        .param("password", "a-secure-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        var saved = users.findByEmailIgnoreCase("alex@example.com").orElseThrow();
        org.assertj.core.api.Assertions.assertThat(passwordEncoder.matches("a-secure-password", saved.getPassword())).isTrue();

        mockMvc.perform(post("/login").with(csrf())
                        .param("email", "alex@example.com")
                        .param("password", "a-secure-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void publicPagesRenderAndPrivatePagesRequireAuthentication() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("home"));
        mockMvc.perform(get("/profile")).andExpect(status().is3xxRedirection());
    }
}