package com.letsplay.api.service;

import com.letsplay.api.dto.*;
import com.letsplay.api.exception.BadRequestException;
import com.letsplay.api.model.User;
import com.letsplay.api.repository.UserRepository;
import com.letsplay.api.security.CustomUserDetails;
import com.letsplay.api.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * Register new user
     */
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        // Create new user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : "ROLE_USER");

        // Save user
        user = userRepository.save(user);

        // Generate token
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtUtil.generateToken(userDetails);

        // Return response
        return new AuthResponse(token, DtoMapper.toUserDTO(user));
    }

    /**
     * Authenticate user and generate token
     */
    public AuthResponse login(LoginRequest request) {
        // Authenticate
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // Get user details
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // Generate token
        String token = jwtUtil.generateToken(userDetails);

        // Return response
        return new AuthResponse(token, DtoMapper.toUserDTO(userDetails.getUser()));
    }
}
