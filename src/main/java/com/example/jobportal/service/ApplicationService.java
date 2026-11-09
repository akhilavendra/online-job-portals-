package com.example.jobportal.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.jobportal.model.ApplicationStatus;
import com.example.jobportal.model.JobApplication;
import com.example.jobportal.model.JobPosting;
import com.example.jobportal.model.UserAccount;
import com.example.jobportal.repository.JobApplicationRepository;
import com.example.jobportal.repository.JobPostingRepository;

@Service
public class ApplicationService {
    private final JobApplicationRepository applications;
    private final JobPostingRepository jobs;

    public ApplicationService(JobApplicationRepository applications, JobPostingRepository jobs) {
        this.applications = applications;
        this.jobs = jobs;
    }

    @Transactional
    public void apply(Long jobId, UserAccount applicant) {
        JobPosting job = jobs.findById(jobId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!job.isActive()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        if (applications.existsByJobIdAndApplicantId(jobId, applicant.getId())) {
            throw new IllegalArgumentException("You have already applied for this role.");
        }
        applications.save(new JobApplication(job, applicant));
    }

    public List<JobApplication> forApplicant(Long applicantId) {
        return applications.findByApplicantIdOrderByAppliedAtDesc(applicantId);
    }

    public List<JobApplication> all() {
        return applications.findAllByOrderByAppliedAtDesc();
    }

    public boolean hasApplied(Long jobId, Long applicantId) {
        return applications.existsByJobIdAndApplicantId(jobId, applicantId);
    }

    @Transactional
    public void updateStatus(Long id, ApplicationStatus status) {
        JobApplication application = applications.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        application.setStatus(status);
    }
}