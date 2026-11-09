package com.example.jobportal.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.jobportal.model.UserAccount;
import com.example.jobportal.service.ApplicationService;
import com.example.jobportal.service.JobService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Controller
public class HomeController {
    private final JobService jobs;
    private final ApplicationService applications;

    public HomeController(JobService jobs, ApplicationService applications) {
        this.jobs = jobs;
        this.applications = applications;
    }

    @GetMapping({"/", "/jobs"})
    public String home(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "") String location,
                       @RequestParam(defaultValue = "") String type, Model model) {
        model.addAttribute("jobs", jobs.search(keyword, location, type));
        model.addAttribute("keyword", keyword);
        model.addAttribute("location", location);
        model.addAttribute("type", type);
        return "home";
    }

    @GetMapping("/jobs/{id}")
    public String jobDetails(@PathVariable Long id, @AuthenticationPrincipal UserAccount account, Model model) {
        var job = jobs.findActive(id);
        model.addAttribute("job", job);
        model.addAttribute("alreadyApplied", account != null && applications.hasApplied(id, account.getId()));
        return "job-detail";
    }
}