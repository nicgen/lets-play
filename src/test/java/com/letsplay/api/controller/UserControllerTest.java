package com.letsplay.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.letsplay.api.dto.UpdateUserRequest;
import com.letsplay.api.dto.UserDTO;
import com.letsplay.api.exception.ForbiddenException;
import com.letsplay.api.exception.ResourceNotFoundException;
import com.letsplay.api.model.User;
import com.letsplay.api.ratelimit.RateLimitFilter;
import com.letsplay.api.ratelimit.RateLimitService;
import com.letsplay.api.security.CustomUserDetails;
import com.letsplay.api.security.JwtAuthenticationEntryPoint;
import com.letsplay.api.security.JwtAuthenticationFilter;
import com.letsplay.api.security.JwtUtil;
import com.letsplay.api.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for UserController
 */
@WebMvcTest(UserController.class)
@Import(com.letsplay.api.config.SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

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

    private UserDTO userDTO;
    private UserDTO adminDTO;
    private UpdateUserRequest updateRequest;

    @BeforeEach
    void setUp() {
        userDTO = new UserDTO();
        userDTO.setId("user123");
        userDTO.setName("John Doe");
        userDTO.setEmail("john@example.com");
        userDTO.setRole("ROLE_USER");

        adminDTO = new UserDTO();
        adminDTO.setId("admin123");
        adminDTO.setName("Admin User");
        adminDTO.setEmail("admin@example.com");
        adminDTO.setRole("ROLE_ADMIN");

        updateRequest = new UpdateUserRequest();
        updateRequest.setName("Updated Name");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllUsersAsAdmin() throws Exception {
        // Setup
        List<UserDTO> users = Arrays.asList(userDTO, adminDTO);
        when(userService.getAllUsers()).thenReturn(users);

        // Execute & Verify
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("user123"))
                .andExpect(jsonPath("$[1].id").value("admin123"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetAllUsersAsUser() throws Exception {
        // Execute & Verify - Should be forbidden for regular users
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetAllUsersWithoutAuth() throws Exception {
        // Execute & Verify - Should be unauthorized without authentication
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "john@example.com", roles = "USER")
    void testGetCurrentUser() throws Exception {
        // Setup
        when(userService.getCurrentUser()).thenReturn(userDTO);

        // Execute & Verify
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("user123"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    @WithMockUser(username = "john@example.com", roles = "USER")
    void testGetUserById() throws Exception {
        // Setup
        when(userService.getUserById("user123")).thenReturn(userDTO);

        // Execute & Verify
        mockMvc.perform(get("/api/users/user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("user123"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    @WithMockUser(username = "john@example.com", roles = "USER")
    void testGetUserByIdNotFound() throws Exception {
        // Setup
        when(userService.getUserById("nonexistent"))
                .thenThrow(new ResourceNotFoundException("User not found"));

        // Execute & Verify
        mockMvc.perform(get("/api/users/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(username = "john@example.com", roles = "USER")
    void testUpdateUserAsOwner() throws Exception {
        // Setup
        UserDTO updatedUser = new UserDTO();
        updatedUser.setId("user123");
        updatedUser.setName("Updated Name");
        updatedUser.setEmail("john@example.com");
        updatedUser.setRole("ROLE_USER");

        when(userService.updateUser(eq("user123"), any(UpdateUserRequest.class)))
                .thenReturn(updatedUser);

        // Execute & Verify
        mockMvc.perform(put("/api/users/user123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    @WithMockUser(username = "john@example.com", roles = "USER")
    void testUpdateOtherUserAsUser() throws Exception {
        // Setup
        when(userService.updateUser(eq("other123"), any(UpdateUserRequest.class)))
                .thenThrow(new ForbiddenException("Access denied"));

        // Execute & Verify
        mockMvc.perform(put("/api/users/other123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void testUpdateUserAsAdmin() throws Exception {
        // Setup
        UserDTO updatedUser = new UserDTO();
        updatedUser.setId("user123");
        updatedUser.setName("Updated by Admin");
        updatedUser.setEmail("john@example.com");
        updatedUser.setRole("ROLE_USER");

        when(userService.updateUser(eq("user123"), any(UpdateUserRequest.class)))
                .thenReturn(updatedUser);

        // Execute & Verify
        mockMvc.perform(put("/api/users/user123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "john@example.com", roles = "USER")
    void testUpdateUserWithInvalidData() throws Exception {
        // Setup - Empty name should fail validation
        updateRequest.setName("");

        // Execute & Verify
        mockMvc.perform(put("/api/users/user123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "john@example.com", roles = "USER")
    void testDeleteUserAsOwner() throws Exception {
        // Setup
        doNothing().when(userService).deleteUser("user123");

        // Execute & Verify
        mockMvc.perform(delete("/api/users/user123"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "john@example.com", roles = "USER")
    void testDeleteOtherUserAsUser() throws Exception {
        // Setup
        doThrow(new ForbiddenException("Access denied"))
                .when(userService).deleteUser("other123");

        // Execute & Verify
        mockMvc.perform(delete("/api/users/other123"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void testDeleteUserAsAdmin() throws Exception {
        // Setup
        doNothing().when(userService).deleteUser("user123");

        // Execute & Verify
        mockMvc.perform(delete("/api/users/user123"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetCurrentUserWithoutAuth() throws Exception {
        // Execute & Verify
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateUserWithoutAuth() throws Exception {
        // Execute & Verify
        mockMvc.perform(put("/api/users/user123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDeleteUserWithoutAuth() throws Exception {
        // Execute & Verify
        mockMvc.perform(delete("/api/users/user123"))
                .andExpect(status().isUnauthorized());
    }
}
