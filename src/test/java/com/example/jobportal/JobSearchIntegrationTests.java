package com.example.jobportal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.example.jobportal.model.JobPosting;
import com.example.jobportal.repository.JobPostingRepository;
import com.example.jobportal.service.JobService;

@DataJpaTest
@Import(JobService.class)
class JobSearchIntegrationTests {
    @Autowired private JobPostingRepository jobs;
    @Autowired private JobService jobService;

    @Test
    void filtersBySkillLocationAndEmploymentType() {
        jobs.save(job("Java Engineer", "Northstar", "Toronto", "Full-time", "Java, Spring Boot"));
        jobs.save(job("Product Designer", "Northstar", "Toronto", "Contract", "Figma, Research"));
        jobs.save(job("Java Engineer", "Brightside", "Austin", "Full-time", "Java, SQL"));

        var matches = jobService.search("spring", "toronto", "Full-time");

        assertThat(matches).hasSize(1);
        assertThat(matches.getFirst().getTitle()).isEqualTo("Java Engineer");
    }

    private JobPosting job(String title, String company, String location, String type, String skills) {
        JobPosting job = new JobPosting();
        job.setTitle(title);
        job.setCompany(company);
        job.setLocation(location);
        job.setEmploymentType(type);
        job.setDescription("A role description for candidates.");
        job.setRequiredSkills(skills);
        return job;
    }
}