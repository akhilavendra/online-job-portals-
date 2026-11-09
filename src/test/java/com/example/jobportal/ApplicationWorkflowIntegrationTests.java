package com.example.jobportal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.example.jobportal.model.JobPosting;
import com.example.jobportal.model.UserAccount;
import com.example.jobportal.model.UserRole;
import com.example.jobportal.repository.JobApplicationRepository;
import com.example.jobportal.repository.JobPostingRepository;
import com.example.jobportal.repository.UserAccountRepository;
import com.example.jobportal.service.ApplicationService;

@DataJpaTest
@Import(ApplicationService.class)
class ApplicationWorkflowIntegrationTests {
    @Autowired private JobApplicationRepository applications;
    @Autowired private JobPostingRepository jobs;
    @Autowired private UserAccountRepository users;
    @Autowired private ApplicationService applicationService;

    @Test
    void candidateCanApplyOnceAndTrackStatusRecord() {
        JobPosting job = jobs.save(job());
        UserAccount candidate = users.save(new UserAccount("candidate@example.com", "encoded", "Candidate User", "+14155552671", UserRole.JOB_SEEKER));

        applicationService.apply(job.getId(), candidate);

        assertThat(applicationService.forApplicant(candidate.getId())).hasSize(1);
        assertThat(applicationService.hasApplied(job.getId(), candidate.getId())).isTrue();
        assertThatThrownBy(() -> applicationService.apply(job.getId(), candidate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already applied");
    }

    @Test
    void cannotApplyToInactiveListing() {
        JobPosting job = job();
        job.setActive(false);
        job = jobs.save(job);
        UserAccount candidate = users.save(new UserAccount("candidate@example.com", "encoded", "Candidate User", "+14155552671", UserRole.JOB_SEEKER));
        JobPosting inactiveJob = job;

        assertThatThrownBy(() -> applicationService.apply(inactiveJob.getId(), candidate))
                .hasMessageContaining("404");
    }

    private JobPosting job() {
        JobPosting job = new JobPosting();
        job.setTitle("Backend Engineer");
        job.setCompany("Example Co");
        job.setLocation("Remote");
        job.setEmploymentType("Full-time");
        job.setDescription("Build reliable services.");
        job.setRequiredSkills("Java, SQL");
        return job;
    }
}