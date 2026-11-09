package com.example.jobportal.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.jobportal.model.JobPosting;
import com.example.jobportal.repository.JobPostingRepository;
import com.example.jobportal.web.form.JobForm;

@Service
public class JobService {
    private final JobPostingRepository jobs;

    public JobService(JobPostingRepository jobs) {
        this.jobs = jobs;
    }

    public List<JobPosting> search(String keyword, String location, String employmentType) {
        return jobs.search(normalize(keyword), normalize(location), normalize(employmentType));
    }

    public JobPosting findActive(Long id) {
        JobPosting job = jobs.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!job.isActive()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return job;
    }

    public JobPosting find(Long id) {
        return jobs.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public List<JobPosting> all() {
        return jobs.findAllByOrderByPostedAtDesc();
    }

    @Transactional
    public JobPosting create(JobForm form) {
        return jobs.save(copy(form, new JobPosting()));
    }

    @Transactional
    public JobPosting update(Long id, JobForm form) {
        return jobs.save(copy(form, find(id)));
    }

    @Transactional
    public void delete(Long id) {
        jobs.delete(find(id));
    }

    public JobForm toForm(JobPosting job) {
        JobForm form = new JobForm();
        form.setTitle(job.getTitle());
        form.setCompany(job.getCompany());
        form.setLocation(job.getLocation());
        form.setEmploymentType(job.getEmploymentType());
        form.setDescription(job.getDescription());
        form.setRequiredSkills(job.getRequiredSkills());
        form.setSalaryRange(job.getSalaryRange());
        form.setActive(job.isActive());
        return form;
    }

    private JobPosting copy(JobForm form, JobPosting job) {
        job.setTitle(form.getTitle().trim());
        job.setCompany(form.getCompany().trim());
        job.setLocation(form.getLocation().trim());
        job.setEmploymentType(form.getEmploymentType().trim());
        job.setDescription(form.getDescription().trim());
        job.setRequiredSkills(form.getRequiredSkills().trim());
        job.setSalaryRange(form.getSalaryRange() == null || form.getSalaryRange().isBlank() ? null : form.getSalaryRange().trim());
        job.setActive(form.isActive());
        return job;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}