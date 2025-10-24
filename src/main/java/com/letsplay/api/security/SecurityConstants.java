package com.letsplay.api.security;

public class SecurityConstants {
    public static final String JWT_SECRET_KEY = "jwt.secret";
    public static final String JWT_EXPIRATION_KEY = "jwt.expiration";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    private SecurityConstants() {
        // Private constructor to prevent instantiation
    }
}
