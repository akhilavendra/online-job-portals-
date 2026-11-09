package com.example.jobportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.jobportal.model.JobApplication;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    boolean existsByJobIdAndApplicantId(Long jobId, Long applicantId);
    List<JobApplication> findByApplicantIdOrderByAppliedAtDesc(Long applicantId);
    List<JobApplication> findAllByOrderByAppliedAtDesc();
}