package com.letsplay.api.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testUserCreation() {
        User user = new User("John Doe", "john@example.com", "password123", "ROLE_USER");

        assertNotNull(user);
        assertEquals("John Doe", user.getName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("ROLE_USER", user.getRole());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
    }

    @Test
    void testUserEquality() {
        User user1 = new User("John", "john@example.com", "pass", "ROLE_USER");
        User user2 = new User("Jane", "john@example.com", "pass", "ROLE_USER");

        user1.setId("123");
        user2.setId("123");

        assertEquals(user1, user2); // Same ID means same user
    }

    @Test
    void testToStringDoesNotExposePassword() {
        User user = new User("John", "john@example.com", "password123", "ROLE_USER");
        String toString = user.toString();

        assertFalse(toString.contains("password123"));
        assertTrue(toString.contains("john@example.com"));
    }
}
