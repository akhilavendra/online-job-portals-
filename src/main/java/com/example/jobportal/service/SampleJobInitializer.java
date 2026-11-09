package com.example.jobportal.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.jobportal.model.JobPosting;
import com.example.jobportal.repository.JobPostingRepository;

@Component
public class SampleJobInitializer implements CommandLineRunner {
    private final JobPostingRepository jobs;

    @Value("${app.seed.sample-jobs:false}")
    private boolean enabled;

    public SampleJobInitializer(JobPostingRepository jobs) {
        this.jobs = jobs;
    }

    @Override
    public void run(String... args) {
        if (!enabled || jobs.count() > 0) return;
        jobs.saveAll(List.of(
                job("Backend Java Engineer", "Northstar Labs", "Toronto, Canada", "Full-time",
                        "Java, Spring Boot, MySQL, REST APIs", "$95,000 - $125,000",
                        "Build dependable services for a growing product team. You will design APIs, improve database performance, and work closely with product and design."),
                job("Product Designer", "Fieldnote Studio", "New York, NY", "Contract",
                        "Figma, Research, Prototyping, Accessibility", "$70 - $90 / hour",
                        "Shape clear, accessible experiences from early research through polished prototypes. Partner with engineering to bring thoughtful product ideas to life."),
                job("Data Analyst", "Open Current", "Remote", "Remote",
                        "SQL, Python, Tableau, Data storytelling", "$82,000 - $108,000",
                        "Turn product and operations data into decisions. Build trusted reporting, investigate trends, and share findings with teams across the company."),
                job("Quality Assurance Engineer", "Brightwell Health", "Berlin, Germany", "Full-time",
                        "Test automation, Java, Playwright, CI/CD", "€68,000 - €84,000",
                        "Help a healthcare team ship reliable software. Create automated checks, investigate defects, and collaborate with developers throughout delivery."),
                job("Customer Success Associate", "Common Thread", "Austin, TX", "Part-time",
                        "Communication, Onboarding, CRM, Problem solving", "$28 - $35 / hour",
                        "Help customers get lasting value from the tools they use every day. Lead onboarding sessions, answer questions, and bring customer feedback back to the team.")));
    }

    private JobPosting job(String title, String company, String location, String type,
                           String skills, String salary, String description) {
        JobPosting job = new JobPosting();
        job.setTitle(title);
        job.setCompany(company);
        job.setLocation(location);
        job.setEmploymentType(type);
        job.setRequiredSkills(skills);
        job.setSalaryRange(salary);
        job.setDescription(description);
        job.setActive(true);
        return job;
    }
}