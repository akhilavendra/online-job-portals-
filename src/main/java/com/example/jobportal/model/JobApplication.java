package com.example.jobportal.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "job_applications", uniqueConstraints = @UniqueConstraint(columnNames = {"job_id", "applicant_id"}))
public class JobApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private JobPosting job;

    @ManyToOne(optional = false)
    @JoinColumn(name = "applicant_id", nullable = false)
    private UserAccount applicant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status = ApplicationStatus.SUBMITTED;

    @Column(nullable = false)
    private LocalDateTime appliedAt;

    protected JobApplication() {
    }

    public JobApplication(JobPosting job, UserAccount applicant) {
        this.job = job;
        this.applicant = applicant;
    }

    @PrePersist
    void setAppliedAt() {
        if (appliedAt == null) appliedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public JobPosting getJob() { return job; }
    public UserAccount getApplicant() { return applicant; }
    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }
    public LocalDateTime getAppliedAt() { return appliedAt; }
}