package com.example.jobportal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.example.jobportal.model.JobPosting;
import com.example.jobportal.model.ApplicationStatus;
import com.example.jobportal.model.JobApplication;
import com.example.jobportal.model.UserAccount;
import com.example.jobportal.model.UserRole;
import com.example.jobportal.repository.JobApplicationRepository;
import com.example.jobportal.repository.JobPostingRepository;
import com.example.jobportal.repository.UserAccountRepository;
import com.example.jobportal.service.ResumeStorageService;

@SpringBootTest(properties = "app.upload-dir=./target/test-uploads")
@AutoConfigureMockMvc
class PortalWebFlowIntegrationTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private UserAccountRepository users;
    @Autowired private JobPostingRepository jobs;
    @Autowired private JobApplicationRepository applications;
    @Autowired private ResumeStorageService resumes;
    @Autowired private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    @AfterEach
    void cleanUpResumes() {
        applications.deleteAll();
        users.findByEmailIgnoreCase("web-candidate@example.com").ifPresent(this::removeCandidate);
        users.findByEmailIgnoreCase("admin-view-candidate@example.com").ifPresent(this::removeCandidate);
        jobs.deleteAll();
    }

    private void removeCandidate(UserAccount candidate) {
        resumes.delete(candidate);
        users.delete(candidate);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminDashboardAndJobEditorRender() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"));
        mockMvc.perform(get("/admin/jobs/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/job-form"));

        mockMvc.perform(post("/admin/jobs").with(csrf())
                        .param("title", "Test Administrator Role")
                        .param("company", "Workwell Test Co")
                        .param("location", "Remote")
                        .param("employmentType", "Full-time")
                        .param("requiredSkills", "Java, SQL")
                        .param("description", "A test role created from the admin workflow."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));
        assertThat(jobs.findAll()).hasSize(1);

        UserAccount candidate = users.save(new UserAccount("admin-view-candidate@example.com", "encoded",
                "Admin View Candidate", "+14155552672", UserRole.JOB_SEEKER));
        JobApplication application = applications.save(new JobApplication(jobs.findAll().getFirst(), candidate));
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"));
        mockMvc.perform(post("/admin/applications/{id}/status", application.getId()).with(csrf())
                        .param("status", "UNDER_REVIEW"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));
        assertThat(applications.findById(application.getId()).orElseThrow().getStatus())
                .isEqualTo(ApplicationStatus.UNDER_REVIEW);
    }

    @Test
    void searchPageFiltersJobsAndCandidateCanApply() throws Exception {
        JobPosting job = jobs.save(job("Java Engineer", "Northstar", "Toronto", "Full-time", "Java, Spring Boot"));
        jobs.save(job("Designer", "Northstar", "Toronto", "Contract", "Figma"));

        mockMvc.perform(get("/jobs").param("keyword", "Spring").param("location", "Toronto").param("type", "Full-time"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("jobs", org.hamcrest.Matchers.hasSize(1)));

        UserAccount candidate = users.save(new UserAccount("web-candidate@example.com",
                passwordEncoder.encode("candidate-password"), "Web Candidate", "+14155552671", UserRole.JOB_SEEKER));
        var login = mockMvc.perform(post("/login").with(csrf())
                        .param("email", candidate.getEmail())
                        .param("password", "candidate-password"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);

        mockMvc.perform(post("/jobs/{id}/apply", job.getId()).session(session).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/applications"));
        assertThat(applications.findByApplicantIdOrderByAppliedAtDesc(candidate.getId())).hasSize(1);
        mockMvc.perform(get("/applications").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("applications"));
        mockMvc.perform(get("/profile").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"));
    }

    @Test
    void signedInCandidateCanUploadAndDownloadResume() throws Exception {
        UserAccount candidate = users.save(new UserAccount("web-candidate@example.com",
                passwordEncoder.encode("candidate-password"), "Web Candidate", "+14155552671", UserRole.JOB_SEEKER));
        var login = mockMvc.perform(post("/login").with(csrf())
                        .param("email", candidate.getEmail())
                        .param("password", "candidate-password"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        MockMultipartFile file = new MockMultipartFile("resume", "candidate.pdf", "application/pdf", "%PDF-1.7 test".getBytes());

        mockMvc.perform(multipart("/profile/resume").file(file).session(session).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));

        UserAccount updated = users.findByEmailIgnoreCase(candidate.getEmail()).orElseThrow();
        assertThat(updated.getResumePath()).endsWith(".pdf");
        mockMvc.perform(get("/profile/resume").session(session))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().contentTypeCompatibleWith("application/octet-stream"));
    }

    @Test
    @WithMockUser(roles = "JOB_SEEKER")
    void jobSeekersCannotOpenAdminDashboard() throws Exception {
        mockMvc.perform(get("/admin")).andExpect(status().isForbidden());
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