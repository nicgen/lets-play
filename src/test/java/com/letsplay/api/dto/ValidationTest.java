package com.letsplay.api.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testLoginRequest_valid() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testLoginRequest_invalidEmail() {
        LoginRequest request = new LoginRequest("invalid-email", "password123");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testLoginRequest_shortPassword() {
        LoginRequest request = new LoginRequest("test@example.com", "short");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testRegisterRequest_valid() {
        RegisterRequest request = new RegisterRequest("John Doe", "john@example.com", "Password123!");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testRegisterRequest_weakPassword_noSpecialChar() {
        RegisterRequest request = new RegisterRequest("John Doe", "john@example.com", "Password123");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("special character")));
    }

    @Test
    void testRegisterRequest_weakPassword_noUppercase() {
        RegisterRequest request = new RegisterRequest("John Doe", "john@example.com", "password123!");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testRegisterRequest_weakPassword_tooShort() {
        RegisterRequest request = new RegisterRequest("John Doe", "john@example.com", "Pass1!");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testCreateProductRequest_valid() {
        CreateProductRequest request = new CreateProductRequest("Laptop", "Gaming laptop", 1299.99);
        Set<ConstraintViolation<CreateProductRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testCreateProductRequest_negativePrice() {
        CreateProductRequest request = new CreateProductRequest("Laptop", "Description", -10.0);
        Set<ConstraintViolation<CreateProductRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testCreateProductRequest_blankName() {
        CreateProductRequest request = new CreateProductRequest("", "Description", 100.0);
        Set<ConstraintViolation<CreateProductRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }
}
