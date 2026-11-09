package com.example.jobportal.web;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.jobportal.model.UserAccount;
import com.example.jobportal.service.AccountService;
import com.example.jobportal.service.ResumeStorageService;
import com.example.jobportal.web.form.ProfileForm;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/profile")
public class ProfileController {
    private final AccountService accounts;
    private final ResumeStorageService resumes;

    public ProfileController(AccountService accounts, ResumeStorageService resumes) {
        this.accounts = accounts;
        this.resumes = resumes;
    }

    @GetMapping
    public String profile(@AuthenticationPrincipal UserAccount account, Model model) {
        model.addAttribute("profileForm", accounts.toProfileForm(account));
        model.addAttribute("account", account);
        return "profile";
    }

    @PostMapping
    public String update(@Valid @ModelAttribute ProfileForm profileForm, BindingResult result,
                         @AuthenticationPrincipal UserAccount account, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("account", account);
            return "profile";
        }
        accounts.updateProfile(account, profileForm);
        redirectAttributes.addFlashAttribute("notice", "Your profile has been updated.");
        return "redirect:/profile";
    }

    @PostMapping("/resume")
    public String uploadResume(@RequestParam("resume") MultipartFile file, @AuthenticationPrincipal UserAccount account,
                               RedirectAttributes redirectAttributes) {
        try {
            resumes.store(account, file);
            redirectAttributes.addFlashAttribute("notice", "Resume uploaded successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/resume/delete")
    public String deleteResume(@AuthenticationPrincipal UserAccount account, RedirectAttributes redirectAttributes) {
        resumes.delete(account);
        redirectAttributes.addFlashAttribute("notice", "Resume removed from your profile.");
        return "redirect:/profile";
    }

    @GetMapping("/resume")
    public ResponseEntity<Resource> downloadResume(@AuthenticationPrincipal UserAccount account) throws IOException {
        Resource resource = resumes.load(account);
        String extension = account.getResumePath().substring(account.getResumePath().lastIndexOf('.') + 1);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resume." + extension + "\"")
                .body(resource);
    }
}