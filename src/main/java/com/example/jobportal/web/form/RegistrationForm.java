package com.example.jobportal.web.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegistrationForm {
    @NotBlank
    @Size(max = 100)
    private String fullName;

    @NotBlank
    @Email
    @Size(max = 190)
    private String email;

    @NotBlank
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Enter a valid phone number with 10 to 15 digits.")
    private String mobile;

    @NotBlank
    @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters.")
    private String password;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}