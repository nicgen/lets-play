package com.letsplay.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.letsplay.api.dto.AuthResponse;
import com.letsplay.api.dto.LoginRequest;
import com.letsplay.api.dto.RegisterRequest;
import com.letsplay.api.dto.UserDTO;
import com.letsplay.api.exception.BadRequestException;
import com.letsplay.api.ratelimit.RateLimitFilter;
import com.letsplay.api.ratelimit.RateLimitService;
import com.letsplay.api.security.JwtAuthenticationEntryPoint;
import com.letsplay.api.security.JwtAuthenticationFilter;
import com.letsplay.api.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController
 */
@WebMvcTest(AuthController.class)
@Import(com.letsplay.api.config.SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private RateLimitService rateLimitService;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private CorsConfigurationSource corsConfigurationSource;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setName("John Doe");
        registerRequest.setEmail("john@example.com");
        registerRequest.setPassword("Password123!");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john@example.com");
        loginRequest.setPassword("Password123!");

        userDTO = new UserDTO();
        userDTO.setId("user123");
        userDTO.setName("John Doe");
        userDTO.setEmail("john@example.com");
        userDTO.setRole("ROLE_USER");

        authResponse = new AuthResponse("jwt-token", userDTO);
    }

    @Test
    void testRegisterSuccess() throws Exception {
        // Setup
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        // Execute & Verify
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.user.id").value("user123"))
                .andExpect(jsonPath("$.user.name").value("John Doe"))
                .andExpect(jsonPath("$.user.email").value("john@example.com"))
                .andExpect(jsonPath("$.user.role").value("ROLE_USER"));
    }

    @Test
    void testRegisterWithDuplicateEmail() throws Exception {
        // Setup
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new BadRequestException("Email already registered"));

        // Execute & Verify
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Email already registered"));
    }

    @Test
    void testRegisterWithInvalidEmail() throws Exception {
        // Setup
        registerRequest.setEmail("invalid-email");

        // Execute & Verify
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegisterWithShortPassword() throws Exception {
        // Setup
        registerRequest.setPassword("Pass1!");

        // Execute & Verify
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegisterWithMissingName() throws Exception {
        // Setup
        registerRequest.setName(null);

        // Execute & Verify
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginSuccess() throws Exception {
        // Setup
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        // Execute & Verify
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.user.email").value("john@example.com"));
    }

    @Test
    void testLoginWithInvalidCredentials() throws Exception {
        // Setup
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Execute & Verify
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLoginWithMissingEmail() throws Exception {
        // Setup
        loginRequest.setEmail(null);

        // Execute & Verify
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginWithMissingPassword() throws Exception {
        // Setup
        loginRequest.setPassword(null);

        // Execute & Verify
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegisterWithWeakPassword() throws Exception {
        // Setup - Password without special character
        registerRequest.setPassword("Password123");

        // Execute & Verify
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }
}
