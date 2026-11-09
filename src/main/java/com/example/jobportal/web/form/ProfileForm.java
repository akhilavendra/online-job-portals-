package com.example.jobportal.web.form;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ProfileForm {
    @NotBlank
    @Size(max = 100)
    private String fullName;

    @NotBlank
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Enter a valid phone number with 10 to 15 digits.")
    private String mobile;

    @Size(max = 120)
    private String headline;

    @Size(max = 120)
    private String location;

    @Size(max = 1000)
    private String skills;

    @Size(max = 120)
    private String qualification;

    @Size(max = 160)
    private String institution;

    @Min(1950)
    @Max(2100)
    private Integer graduationYear;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getHeadline() { return headline; }
    public void setHeadline(String headline) { this.headline = headline; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }
    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution; }
    public Integer getGraduationYear() { return graduationYear; }
    public void setGraduationYear(Integer graduationYear) { this.graduationYear = graduationYear; }
}