package com.example.jobportal.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.jobportal.model.UserAccount;
import com.example.jobportal.service.AccountService;
import com.example.jobportal.web.form.RegistrationForm;

import jakarta.validation.Valid;

@Controller
public class AuthController {
    private final AccountService accounts;

    public AuthController(AccountService accounts) {
        this.accounts = accounts;
    }

    @GetMapping("/register")
    public String registerPage(@AuthenticationPrincipal UserAccount account, Model model) {
        if (account != null) return "redirect:/";
        model.addAttribute("registrationForm", new RegistrationForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegistrationForm registrationForm, BindingResult result,
                           Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) return "register";
        try {
            accounts.register(registrationForm);
        } catch (IllegalArgumentException exception) {
            result.rejectValue("email", "duplicate", exception.getMessage());
            return "register";
        }
        redirectAttributes.addFlashAttribute("notice", "Your account is ready. Sign in to complete your profile.");
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(@AuthenticationPrincipal UserAccount account) {
        return account == null ? "login" : "redirect:/";
    }
}