package com.letsplay.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a string does not contain MongoDB operators
 * that could be used for injection attacks.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MongoSafeValidator.class)
public @interface MongoSafe {
    String message() default "Input contains invalid characters for MongoDB";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
