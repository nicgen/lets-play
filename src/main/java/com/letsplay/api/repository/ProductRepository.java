package com.letsplay.api.repository;

import com.letsplay.api.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    /**
     * Find all products owned by a specific user
     * Critical for user-product relationship
     */
    List<Product> findByUserId(String userId);

    /**
     * Find products by name (exact match)
     */
    Optional<Product> findByName(String name);

    /**
     * Find products where name contains search term (case-insensitive)
     * Search functionality
     */
    @Query("{ 'name': { $regex: ?0, $options: 'i' } }")
    List<Product> findByNameContainingIgnoreCase(String name);

    /**
     * Find products within price range
     * Filter functionality
     */
    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);

    /**
     * Find products by user ID and price range
     * Combined filtering for user's products
     */
    List<Product> findByUserIdAndPriceBetween(String userId, Double minPrice, Double maxPrice);

    /**
     * Delete all products for a specific user
     * Cascade deletion when user is deleted
     */
    void deleteByUserId(String userId);

    /**
     * Count products for a specific user
     * Analytics functionality
     */
    long countByUserId(String userId);

    /**
     * Check if product exists with given name and user ID
     * Prevent duplicate product names per user
     */
    boolean existsByNameAndUserId(String name, String userId);

    /**
     * Find products with price greater than specified value
     */
    List<Product> findByPriceGreaterThan(Double price);

    /**
     * Find products with price less than specified value
     */
    List<Product> findByPriceLessThan(Double price);
}
