package com.example.jobportal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import com.example.jobportal.model.UserAccount;
import com.example.jobportal.model.UserRole;
import com.example.jobportal.repository.UserAccountRepository;
import com.example.jobportal.service.ResumeStorageService;

class ResumeStorageTests {
    @TempDir java.nio.file.Path tempDirectory;

    @Test
    void storesPdfWithGeneratedName() {
        UserAccountRepository users = mock(UserAccountRepository.class);
        when(users.save(org.mockito.ArgumentMatchers.any(UserAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        ResumeStorageService storage = new ResumeStorageService(users, tempDirectory.toString());
        UserAccount account = account();
        MockMultipartFile file = new MockMultipartFile("resume", "candidate.pdf", "application/pdf",
                "%PDF-1.7 sample".getBytes());

        storage.store(account, file);

        assertThat(account.getResumePath()).endsWith(".pdf").doesNotContain("candidate");
        assertThat(Files.exists(tempDirectory.resolve(account.getResumePath()))).isTrue();
    }

    @Test
    void rejectsMismatchedFileSignature() {
        UserAccountRepository users = mock(UserAccountRepository.class);
        ResumeStorageService storage = new ResumeStorageService(users, tempDirectory.toString());
        MockMultipartFile file = new MockMultipartFile("resume", "candidate.pdf", "application/pdf", "not a PDF".getBytes());

        assertThatThrownBy(() -> storage.store(account(), file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not match");
    }

    private UserAccount account() {
        return new UserAccount("candidate@example.com", "encoded", "Candidate User", "+14155552671", UserRole.JOB_SEEKER);
    }
}