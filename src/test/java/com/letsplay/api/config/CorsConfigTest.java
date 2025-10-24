package com.letsplay.api.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CorsConfig
 *
 * Note: This is a unit test that does NOT require MongoDB or full Spring context.
 * It tests the CORS configuration bean creation directly.
 */
class CorsConfigTest {

    private CorsConfig corsConfig;

    @BeforeEach
    void setUp() {
        corsConfig = new CorsConfig();
    }

    @Test
    void testCorsConfigurationSourceBeanCreation() {
        // Set properties using reflection (simulating @Value injection)
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", "http://localhost:3000,http://localhost:4200");
        ReflectionTestUtils.setField(corsConfig, "maxAge", 3600L);

        // Create the CORS configuration source
        CorsConfigurationSource source = corsConfig.corsConfigurationSource();

        assertNotNull(source);
        assertTrue(source instanceof UrlBasedCorsConfigurationSource);
    }

    @Test
    void testCorsConfigurationWithDefaultValues() {
        // Use default values
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", "http://localhost:3000,http://localhost:4200,http://localhost:8080");
        ReflectionTestUtils.setField(corsConfig, "maxAge", 3600L);

        CorsConfigurationSource source = corsConfig.corsConfigurationSource();
        assertNotNull(source);
    }

    @Test
    void testCorsConfigurationWithSingleOrigin() {
        // Test with a single origin
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", "http://localhost:3000");
        ReflectionTestUtils.setField(corsConfig, "maxAge", 7200L);

        CorsConfigurationSource source = corsConfig.corsConfigurationSource();
        assertNotNull(source);
    }

    @Test
    void testCorsConfigurationWithMultipleOrigins() {
        // Test with multiple origins (including production URLs)
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins",
            "http://localhost:3000,https://example.com,https://app.example.com");
        ReflectionTestUtils.setField(corsConfig, "maxAge", 1800L);

        CorsConfigurationSource source = corsConfig.corsConfigurationSource();
        assertNotNull(source);
    }

    @Test
    void testCorsConfigurationWithEmptyOrigins() {
        // Test that empty origins string doesn't break configuration
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", "");
        ReflectionTestUtils.setField(corsConfig, "maxAge", 3600L);

        CorsConfigurationSource source = corsConfig.corsConfigurationSource();
        assertNotNull(source);
    }
}
