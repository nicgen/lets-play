package com.letsplay.api.model;

import java.util.regex.Pattern;

public class ModelValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern MONGODB_ID_PATTERN = Pattern.compile("^[a-fA-F0-9]{24}$");

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidRole(String role) {
        return role != null && (role.equals("ROLE_USER") || role.equals("ROLE_ADMIN"));
    }

    public static boolean isValidMongoId(String id) {
        return id != null && MONGODB_ID_PATTERN.matcher(id).matches();
    }

    public static boolean isValidPrice(Double price) {
        return price != null && price >= 0;
    }

    public static boolean isValidString(String str, int minLength, int maxLength) {
        return str != null &&
               str.length() >= minLength &&
               str.length() <= maxLength &&
               !str.trim().isEmpty();
    }

    private ModelValidator() {
        // Private constructor to prevent instantiation
    }
}
