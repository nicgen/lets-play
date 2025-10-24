package com.letsplay.api.repository;

import com.letsplay.api.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    /**
     * Find user by email address
     * Used for authentication and duplicate checking
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if user exists by email
     * Useful for validation before registration
     */
    boolean existsByEmail(String email);

    /**
     * Find users by role
     * Admin functionality to list users by role
     */
    List<User> findByRole(String role);

    /**
     * Find users whose name contains the given string (case-insensitive)
     * Search functionality
     */
    @Query("{ 'name': { $regex: ?0, $options: 'i' } }")
    List<User> findByNameContainingIgnoreCase(String name);

    /**
     * Delete user by email
     * Alternative deletion method
     */
    void deleteByEmail(String email);

    /**
     * Count users by role
     * Analytics/reporting functionality
     */
    long countByRole(String role);
}
