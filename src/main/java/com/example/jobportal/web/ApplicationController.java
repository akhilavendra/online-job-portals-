package com.example.jobportal.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.jobportal.model.UserAccount;
import com.example.jobportal.service.ApplicationService;

@Controller
public class ApplicationController {
    private final ApplicationService applications;

    public ApplicationController(ApplicationService applications) {
        this.applications = applications;
    }

    @PostMapping("/jobs/{id}/apply")
    public String apply(@PathVariable Long id, @AuthenticationPrincipal UserAccount account, RedirectAttributes redirectAttributes) {
        try {
            applications.apply(id, account);
            redirectAttributes.addFlashAttribute("notice", "Application submitted. You can track its progress here.");
            return "redirect:/applications";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/jobs/" + id;
        }
    }

    @GetMapping("/applications")
    public String applications(@AuthenticationPrincipal UserAccount account, Model model) {
        model.addAttribute("applications", applications.forApplicant(account.getId()));
        return "applications";
    }
}