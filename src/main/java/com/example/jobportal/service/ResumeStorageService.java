package com.example.jobportal.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.jobportal.model.UserAccount;
import com.example.jobportal.repository.UserAccountRepository;

@Service
public class ResumeStorageService {
    private final UserAccountRepository users;
    private final Path uploadDirectory;

    @Value("${app.max-resume-size:5242880}")
    private long maxSize = 5 * 1024 * 1024;

    public ResumeStorageService(UserAccountRepository users, @Value("${app.upload-dir:./uploads}") String uploadDirectory) {
        this.users = users;
        this.uploadDirectory = Paths.get(uploadDirectory).toAbsolutePath().normalize();
    }

    public void store(UserAccount account, MultipartFile file) {
        if (file.isEmpty() || file.getSize() > maxSize) throw new IllegalArgumentException("Choose a resume file smaller than 5 MB.");
        String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String extension = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (!extension.equals("pdf") && !extension.equals("doc") && !extension.equals("docx")) {
            throw new IllegalArgumentException("Resume must be a PDF, DOC or DOCX file.");
        }
        try {
            byte[] content = file.getBytes();
            if (!hasExpectedSignature(extension, content)) throw new IllegalArgumentException("The uploaded file does not match its file type.");
            Files.createDirectories(uploadDirectory);
            String storedName = UUID.randomUUID() + "." + extension;
            Path destination = uploadDirectory.resolve(storedName).normalize();
            if (!destination.startsWith(uploadDirectory)) throw new IllegalArgumentException("Invalid file name.");
            Files.write(destination, content);
            if (account.getResumePath() != null) Files.deleteIfExists(resolveStoredFile(account.getResumePath()));
            account.setResumePath(storedName);
            users.save(account);
        } catch (IOException exception) {
            throw new IllegalStateException("Resume could not be stored. Please try again.", exception);
        }
    }

    public Resource load(UserAccount account) {
        if (account.getResumePath() == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        try {
            Path path = resolveStoredFile(account.getResumePath());
            if (!Files.isRegularFile(path)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            return new UrlResource(path.toUri());
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    public void delete(UserAccount account) {
        if (account.getResumePath() == null) return;
        try {
            Files.deleteIfExists(resolveStoredFile(account.getResumePath()));
            account.setResumePath(null);
            users.save(account);
        } catch (IOException exception) {
            throw new IllegalStateException("Resume could not be deleted. Please try again.", exception);
        }
    }

    private boolean hasExpectedSignature(String extension, byte[] bytes) {
        if (extension.equals("pdf")) return bytes.length >= 5 && bytes[0] == '%' && bytes[1] == 'P' && bytes[2] == 'D' && bytes[3] == 'F' && bytes[4] == '-';
        if (extension.equals("doc")) return bytes.length >= 4 && (bytes[0] & 0xff) == 0xd0 && (bytes[1] & 0xff) == 0xcf && (bytes[2] & 0xff) == 0x11 && (bytes[3] & 0xff) == 0xe0;
        return bytes.length >= 4 && bytes[0] == 'P' && bytes[1] == 'K' && bytes[2] == 3 && bytes[3] == 4;
    }

    private Path resolveStoredFile(String filename) {
        Path path = uploadDirectory.resolve(filename).normalize();
        if (!path.startsWith(uploadDirectory)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return path;
    }
}