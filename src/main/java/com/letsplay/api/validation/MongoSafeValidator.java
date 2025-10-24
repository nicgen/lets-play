package com.letsplay.api.validation;

import com.letsplay.api.util.MongoSanitizer;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for @MongoSafe annotation.
 * Ensures input does not contain MongoDB operators.
 */
public class MongoSafeValidator implements ConstraintValidator<MongoSafe, String> {

    @Override
    public void initialize(MongoSafe constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Null values are handled by @NotNull, allow them here
        if (value == null) {
            return true;
        }

        return MongoSanitizer.isValidInput(value);
    }
}
