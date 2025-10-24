package com.letsplay.api.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ModelValidatorTest {

    @Test
    void testEmailValidation() {
        assertTrue(ModelValidator.isValidEmail("test@example.com"));
        assertTrue(ModelValidator.isValidEmail("user.name+tag@example.co.uk"));

        assertFalse(ModelValidator.isValidEmail("invalid"));
        assertFalse(ModelValidator.isValidEmail("@example.com"));
        assertFalse(ModelValidator.isValidEmail("test@"));
        assertFalse(ModelValidator.isValidEmail(null));
    }

    @Test
    void testRoleValidation() {
        assertTrue(ModelValidator.isValidRole("ROLE_USER"));
        assertTrue(ModelValidator.isValidRole("ROLE_ADMIN"));

        assertFalse(ModelValidator.isValidRole("USER"));
        assertFalse(ModelValidator.isValidRole("ADMIN"));
        assertFalse(ModelValidator.isValidRole("ROLE_SUPERUSER"));
        assertFalse(ModelValidator.isValidRole(null));
    }

    @Test
    void testMongoIdValidation() {
        assertTrue(ModelValidator.isValidMongoId("507f1f77bcf86cd799439011"));
        assertTrue(ModelValidator.isValidMongoId("123456789012345678901234"));

        assertFalse(ModelValidator.isValidMongoId("invalid"));
        assertFalse(ModelValidator.isValidMongoId("123")); // Too short
        assertFalse(ModelValidator.isValidMongoId(null));
    }

    @Test
    void testPriceValidation() {
        assertTrue(ModelValidator.isValidPrice(0.0));
        assertTrue(ModelValidator.isValidPrice(99.99));
        assertTrue(ModelValidator.isValidPrice(1000000.0));

        assertFalse(ModelValidator.isValidPrice(-1.0));
        assertFalse(ModelValidator.isValidPrice(-0.01));
        assertFalse(ModelValidator.isValidPrice(null));
    }

    @Test
    void testStringValidation() {
        assertTrue(ModelValidator.isValidString("John Doe", 2, 50));
        assertTrue(ModelValidator.isValidString("A", 1, 10));

        assertFalse(ModelValidator.isValidString("", 1, 10)); // Empty
        assertFalse(ModelValidator.isValidString("   ", 1, 10)); // Only whitespace
        assertFalse(ModelValidator.isValidString("A", 2, 10)); // Too short
        assertFalse(ModelValidator.isValidString("VeryLongString", 1, 5)); // Too long
        assertFalse(ModelValidator.isValidString(null, 1, 10)); // Null
    }
}
