package com.letsplay.api.repository;

import com.letsplay.api.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("John Doe", "john@example.com", "password123", "ROLE_USER");
        userRepository.save(testUser);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void testSaveUser() {
        User newUser = new User("Jane Doe", "jane@example.com", "password456", "ROLE_USER");
        User saved = userRepository.save(newUser);

        assertNotNull(saved.getId());
        assertEquals("Jane Doe", saved.getName());
        assertEquals("jane@example.com", saved.getEmail());
    }

    @Test
    void testFindByEmail() {
        Optional<User> found = userRepository.findByEmail("john@example.com");

        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
    }

    @Test
    void testFindByEmail_NotFound() {
        Optional<User> found = userRepository.findByEmail("notfound@example.com");

        assertFalse(found.isPresent());
    }

    @Test
    void testExistsByEmail() {
        assertTrue(userRepository.existsByEmail("john@example.com"));
        assertFalse(userRepository.existsByEmail("notfound@example.com"));
    }

    @Test
    void testFindByRole() {
        User admin = new User("Admin User", "admin@example.com", "password", "ROLE_ADMIN");
        userRepository.save(admin);

        List<User> users = userRepository.findByRole("ROLE_USER");
        List<User> admins = userRepository.findByRole("ROLE_ADMIN");

        assertEquals(1, users.size());
        assertEquals(1, admins.size());
        assertEquals("John Doe", users.get(0).getName());
        assertEquals("Admin User", admins.get(0).getName());
    }

    @Test
    void testFindByNameContainingIgnoreCase() {
        User another = new User("Johnny Cash", "johnny@example.com", "password", "ROLE_USER");
        userRepository.save(another);

        List<User> found = userRepository.findByNameContainingIgnoreCase("john");

        assertEquals(2, found.size());
    }

    @Test
    void testCountByRole() {
        userRepository.save(new User("User2", "user2@example.com", "pass", "ROLE_USER"));
        userRepository.save(new User("Admin1", "admin1@example.com", "pass", "ROLE_ADMIN"));

        long userCount = userRepository.countByRole("ROLE_USER");
        long adminCount = userRepository.countByRole("ROLE_ADMIN");

        assertEquals(2, userCount);
        assertEquals(1, adminCount);
    }

    @Test
    void testDeleteByEmail() {
        userRepository.deleteByEmail("john@example.com");

        Optional<User> found = userRepository.findByEmail("john@example.com");
        assertFalse(found.isPresent());
    }

    @Test
    void testUniqueEmailConstraint() {
        User duplicate = new User("Another John", "john@example.com", "pass", "ROLE_USER");

        assertThrows(Exception.class, () -> {
            userRepository.save(duplicate);
        });
    }
}
