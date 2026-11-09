package com.example.jobportal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.example.jobportal.web.form.RegistrationForm;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class RegistrationValidationTests {
    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void acceptsValidEmailAndInternationalMobile() {
        RegistrationForm form = form("alex@example.com", "+14155552671");
        assertThat(validator.validate(form)).isEmpty();
    }

    @Test
    void rejectsMalformedEmailAndPhone() {
        RegistrationForm form = form("not-an-email", "555-12");
        Set<?> violations = validator.validate(form);
        assertThat(violations).hasSize(2);
    }

    private RegistrationForm form(String email, String mobile) {
        RegistrationForm form = new RegistrationForm();
        form.setFullName("Alex Morgan");
        form.setEmail(email);
        form.setMobile(mobile);
        form.setPassword("a-secure-password");
        return form;
    }
}