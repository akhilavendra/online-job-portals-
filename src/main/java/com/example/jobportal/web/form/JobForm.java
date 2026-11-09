package com.example.jobportal.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class JobForm {
    @NotBlank
    @Size(max = 120)
    private String title;

    @NotBlank
    @Size(max = 120)
    private String company;

    @NotBlank
    @Size(max = 120)
    private String location;

    @NotBlank
    @Size(max = 40)
    private String employmentType;

    @NotBlank
    @Size(max = 3000)
    private String description;

    @NotBlank
    @Size(max = 500)
    private String requiredSkills;

    @Size(max = 80)
    private String salaryRange;

    private boolean active = true;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }
    public String getSalaryRange() { return salaryRange; }
    public void setSalaryRange(String salaryRange) { this.salaryRange = salaryRange; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}