package com.letsplay.api.util;

import java.util.regex.Pattern;

/**
 * Utility class for sanitizing user input to prevent MongoDB injection attacks.
 *
 * MongoDB operators like $ and . can be exploited if included in user input.
 * This class removes these characters to prevent injection attacks.
 */
public class MongoSanitizer {

    // Pattern to detect MongoDB operators
    private static final Pattern MONGO_OPERATOR_PATTERN = Pattern.compile("[$\\.]");

    // Pattern to detect control characters
    private static final Pattern CONTROL_CHAR_PATTERN = Pattern.compile("[\\x00-\\x1F\\x7F]");

    /**
     * Sanitize input by removing MongoDB operators and control characters
     *
     * @param input The input string to sanitize
     * @return Sanitized string with dangerous characters removed
     */
    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }

        // Remove MongoDB operators ($, .)
        String sanitized = MONGO_OPERATOR_PATTERN.matcher(input).replaceAll("");

        // Remove control characters
        sanitized = CONTROL_CHAR_PATTERN.matcher(sanitized).replaceAll("");

        // Trim whitespace
        return sanitized.trim();
    }

    /**
     * Check if input contains valid characters (no MongoDB operators)
     *
     * @param input The input string to validate
     * @return true if input is safe, false if it contains dangerous characters
     */
    public static boolean isValidInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        // Check for MongoDB operators
        if (MONGO_OPERATOR_PATTERN.matcher(input).find()) {
            return false;
        }

        // Check for control characters
        if (CONTROL_CHAR_PATTERN.matcher(input).find()) {
            return false;
        }

        return true;
    }

    /**
     * Validate and sanitize input, throwing exception if invalid
     *
     * @param input The input string to process
     * @param fieldName Name of the field for error messages
     * @return Sanitized string
     * @throws IllegalArgumentException if input contains dangerous characters
     */
    public static String sanitizeAndValidate(String input, String fieldName) {
        if (!isValidInput(input)) {
            throw new IllegalArgumentException(
                fieldName + " contains invalid characters. MongoDB operators ($, .) are not allowed."
            );
        }
        return sanitize(input);
    }

    private MongoSanitizer() {
        // Private constructor to prevent instantiation
    }
}
