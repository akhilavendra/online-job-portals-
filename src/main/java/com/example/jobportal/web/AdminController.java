package com.example.jobportal.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.jobportal.model.ApplicationStatus;
import com.example.jobportal.service.ApplicationService;
import com.example.jobportal.service.JobService;
import com.example.jobportal.web.form.JobForm;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final JobService jobs;
    private final ApplicationService applications;

    public AdminController(JobService jobs, ApplicationService applications) {
        this.jobs = jobs;
        this.applications = applications;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("jobs", jobs.all());
        model.addAttribute("applications", applications.all());
        model.addAttribute("statuses", ApplicationStatus.values());
        return "admin/dashboard";
    }

    @GetMapping("/jobs/new")
    public String newJob(Model model) {
        model.addAttribute("jobForm", new JobForm());
        model.addAttribute("editing", false);
        return "admin/job-form";
    }

    @PostMapping("/jobs")
    public String create(@Valid @ModelAttribute JobForm jobForm, BindingResult result, Model model,
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("editing", false);
            return "admin/job-form";
        }
        jobs.create(jobForm);
        redirectAttributes.addFlashAttribute("notice", "Job listing created.");
        return "redirect:/admin";
    }

    @GetMapping("/jobs/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("jobForm", jobs.toForm(jobs.find(id)));
        model.addAttribute("jobId", id);
        model.addAttribute("editing", true);
        return "admin/job-form";
    }

    @PostMapping("/jobs/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute JobForm jobForm, BindingResult result,
                         Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("editing", true);
            model.addAttribute("jobId", id);
            return "admin/job-form";
        }
        jobs.update(id, jobForm);
        redirectAttributes.addFlashAttribute("notice", "Job listing updated.");
        return "redirect:/admin";
    }

    @PostMapping("/jobs/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            jobs.delete(id);
            redirectAttributes.addFlashAttribute("notice", "Job listing deleted.");
        } catch (org.springframework.dao.DataIntegrityViolationException exception) {
            redirectAttributes.addFlashAttribute("error", "This job has applications and cannot be deleted. Mark it inactive instead.");
        }
        return "redirect:/admin";
    }

    @PostMapping("/applications/{id}/status")
    public String updateStatus(@PathVariable Long id, ApplicationStatus status, RedirectAttributes redirectAttributes) {
        applications.updateStatus(id, status);
        redirectAttributes.addFlashAttribute("notice", "Application status updated.");
        return "redirect:/admin";
    }
}