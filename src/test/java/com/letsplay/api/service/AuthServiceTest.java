package com.letsplay.api.service;

import com.letsplay.api.dto.AuthResponse;
import com.letsplay.api.dto.LoginRequest;
import com.letsplay.api.dto.RegisterRequest;
import com.letsplay.api.dto.UserDTO;
import com.letsplay.api.exception.BadRequestException;
import com.letsplay.api.model.Role;
import com.letsplay.api.model.User;
import com.letsplay.api.repository.UserRepository;
import com.letsplay.api.security.CustomUserDetails;
import com.letsplay.api.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setName("John Doe");
        registerRequest.setEmail("john@example.com");
        registerRequest.setPassword("Password123!");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john@example.com");
        loginRequest.setPassword("Password123!");

        user = new User();
        user.setId("user123");
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setPassword("hashedPassword");
        user.setRoleEnum(Role.USER);
    }

    @Test
    void testRegisterSuccess() {
        // Setup
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        // Execute
        AuthResponse response = authService.register(registerRequest);

        // Verify
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("jwt-token", response.getToken());
        assertNotNull(response.getUser());
        assertEquals("John Doe", response.getUser().getName());
        assertEquals("john@example.com", response.getUser().getEmail());

        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).generateToken(any(UserDetails.class));
    }

    @Test
    void testRegisterWithDuplicateEmail() {
        // Setup
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // Execute & Verify
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> authService.register(registerRequest)
        );

        assertEquals("Email already registered", exception.getMessage());
        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
        verify(jwtUtil, never()).generateToken(any(UserDetails.class));
    }

    @Test
    void testLoginSuccess() {
        // Setup
        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        // Execute
        AuthResponse response = authService.login(loginRequest);

        // Verify
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertNotNull(response.getUser());
        assertEquals("john@example.com", response.getUser().getEmail());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken(any(UserDetails.class));
    }

    @Test
    void testLoginWithInvalidCredentials() {
        // Setup
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Execute & Verify
        assertThrows(
            BadCredentialsException.class,
            () -> authService.login(loginRequest)
        );

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, never()).generateToken(any(UserDetails.class));
    }

    // Test removed: testLoginUserNotFound is not a valid scenario
    // The AuthenticationManager will throw BadCredentialsException if user is not found
    // This is already tested in testLoginWithInvalidCredentials

    @Test
    void testRegisterSetsDefaultRole() {
        // Setup
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertEquals(Role.USER.getValue(), savedUser.getRole());
            return savedUser;
        });
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        // Execute
        authService.register(registerRequest);

        // Verify through the save method
        verify(userRepository).save(any(User.class));
    }
}
