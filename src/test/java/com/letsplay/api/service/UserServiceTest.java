package com.letsplay.api.service;

import com.letsplay.api.dto.UpdateUserRequest;
import com.letsplay.api.dto.UserDTO;
import com.letsplay.api.exception.ForbiddenException;
import com.letsplay.api.exception.ResourceNotFoundException;
import com.letsplay.api.model.Role;
import com.letsplay.api.model.User;
import com.letsplay.api.repository.UserRepository;
import com.letsplay.api.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;

/**
 * Unit tests for UserService
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    private User user;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId("user123");
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setPassword("hashedPassword");
        user.setRoleEnum(Role.USER);

        userDetails = new CustomUserDetails(user);

        // Setup security context
        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.isAuthenticated()).thenReturn(true);
        lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
    }

    @Test
    void testGetAllUsers() {
        // Setup
        User user2 = new User();
        user2.setId("user456");
        user2.setName("Jane Doe");
        user2.setEmail("jane@example.com");
        user2.setRoleEnum(Role.USER);

        when(userRepository.findAll()).thenReturn(Arrays.asList(user, user2));

        // Execute
        List<UserDTO> users = userService.getAllUsers();

        // Verify
        assertNotNull(users);
        assertEquals(2, users.size());
        assertEquals("John Doe", users.get(0).getName());
        assertEquals("Jane Doe", users.get(1).getName());
        verify(userRepository).findAll();
    }

    @Test
    void testGetUserById() {
        // Setup
        when(userRepository.findById("user123")).thenReturn(Optional.of(user));

        // Execute
        UserDTO result = userService.getUserById("user123");

        // Verify
        assertNotNull(result);
        assertEquals("user123", result.getId());
        assertEquals("John Doe", result.getName());
        verify(userRepository).findById("user123");
    }

    @Test
    void testGetUserByIdNotFound() {
        // Setup
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // Execute & Verify
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> userService.getUserById("nonexistent")
        );

        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository).findById("nonexistent");
    }

    @Test
    void testGetCurrentUser() {
        // Setup
        when(userRepository.findById("user123")).thenReturn(Optional.of(user));

        // Execute
        UserDTO result = userService.getCurrentUser();

        // Verify
        assertNotNull(result);
        assertEquals("user123", result.getId());
        assertEquals("john@example.com", result.getEmail());
        verify(userRepository).findById("user123");
    }

    @Test
    void testUpdateUserAsOwner() {
        // Setup
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("John Updated");

        when(userRepository.findById("user123")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Execute
        UserDTO result = userService.updateUser("user123", request);

        // Verify
        assertNotNull(result);
        assertEquals("John Updated", result.getName());
        verify(userRepository).findById("user123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateUserAsAdmin() {
        // Setup - Make current user an admin
        User adminUser = new User();
        adminUser.setId("admin123");
        adminUser.setRoleEnum(Role.ADMIN);
        CustomUserDetails adminDetails = new CustomUserDetails(adminUser);

        lenient().when(authentication.getPrincipal()).thenReturn(adminDetails);
        when(authentication.getAuthorities()).thenAnswer(invocation ->
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));

        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated by Admin");

        when(userRepository.findById("user123")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Execute
        UserDTO result = userService.updateUser("user123", request);

        // Verify
        assertNotNull(result);
        verify(userRepository).findById("user123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateUserUnauthorized() {
        // Setup - Try to update a different user without admin rights
        User otherUser = new User();
        otherUser.setId("other123");
        otherUser.setName("Other User");
        otherUser.setRoleEnum(Role.USER);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Hacker Attempt");

        when(userRepository.findById("other123")).thenReturn(Optional.of(otherUser));
        when(authentication.getAuthorities()).thenAnswer(invocation ->
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        // Execute & Verify
        ForbiddenException exception = assertThrows(
            ForbiddenException.class,
            () -> userService.updateUser("other123", request)
        );

        assertTrue(exception.getMessage().contains("Access denied"));
        verify(userRepository).findById("other123");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testDeleteUserAsOwner() {
        // Setup - owner deleting their own account
        when(userRepository.existsById("user123")).thenReturn(true);
        when(authentication.getAuthorities()).thenAnswer(invocation ->
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        doNothing().when(userRepository).deleteById("user123");

        // Execute
        userService.deleteUser("user123");

        // Verify
        verify(userRepository).existsById("user123");
        verify(userRepository).deleteById("user123");
    }

    @Test
    void testDeleteUserUnauthorized() {
        // Setup - user trying to delete another user's account
        when(userRepository.existsById("other123")).thenReturn(true);
        when(authentication.getAuthorities()).thenAnswer(invocation ->
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        // Execute & Verify
        ForbiddenException exception = assertThrows(
            ForbiddenException.class,
            () -> userService.deleteUser("other123")
        );

        assertTrue(exception.getMessage().contains("Access denied"));
        verify(userRepository).existsById("other123");
        verify(userRepository, never()).deleteById(anyString());
    }

    @Test
    void testDeleteUserNotFound() {
        // Setup
        when(userRepository.existsById("nonexistent")).thenReturn(false);

        // Execute & Verify
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> userService.deleteUser("nonexistent")
        );

        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository).existsById("nonexistent");
        verify(userRepository, never()).deleteById(anyString());
    }
}
