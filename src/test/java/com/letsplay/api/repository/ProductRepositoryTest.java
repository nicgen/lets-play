package com.letsplay.api.repository;

import com.letsplay.api.model.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct;
    private final String userId = "user123";

    @BeforeEach
    void setUp() {
        testProduct = new Product("Laptop", "Gaming laptop", 1299.99, userId);
        productRepository.save(testProduct);
    }

    @AfterEach
    void tearDown() {
        productRepository.deleteAll();
    }

    @Test
    void testSaveProduct() {
        Product newProduct = new Product("Mouse", "Wireless mouse", 29.99, userId);
        Product saved = productRepository.save(newProduct);

        assertNotNull(saved.getId());
        assertEquals("Mouse", saved.getName());
        assertEquals(29.99, saved.getPrice());
    }

    @Test
    void testFindByUserId() {
        Product another = new Product("Keyboard", "Mechanical keyboard", 99.99, userId);
        productRepository.save(another);

        List<Product> products = productRepository.findByUserId(userId);

        assertEquals(2, products.size());
    }

    @Test
    void testFindByUserId_DifferentUser() {
        Product otherUserProduct = new Product("Item", "Description", 50.0, "user456");
        productRepository.save(otherUserProduct);

        List<Product> products = productRepository.findByUserId(userId);

        assertEquals(1, products.size());
        assertEquals("Laptop", products.get(0).getName());
    }

    @Test
    void testFindByPriceBetween() {
        productRepository.save(new Product("Cheap Item", "Low price", 10.0, userId));
        productRepository.save(new Product("Expensive Item", "High price", 5000.0, userId));

        List<Product> midRange = productRepository.findByPriceBetween(50.0, 2000.0);

        assertEquals(1, midRange.size());
        assertEquals("Laptop", midRange.get(0).getName());
    }

    @Test
    void testFindByUserIdAndPriceBetween() {
        productRepository.save(new Product("Item1", "Desc", 100.0, userId));
        productRepository.save(new Product("Item2", "Desc", 200.0, "user456"));

        List<Product> products = productRepository.findByUserIdAndPriceBetween(userId, 50.0, 150.0);

        assertEquals(1, products.size());
        assertEquals("Item1", products.get(0).getName());
    }

    @Test
    void testFindByNameContainingIgnoreCase() {
        productRepository.save(new Product("Gaming Laptop Pro", "High-end laptop", 2000.0, userId));

        List<Product> found = productRepository.findByNameContainingIgnoreCase("laptop");

        assertEquals(2, found.size());
    }

    @Test
    void testCountByUserId() {
        productRepository.save(new Product("Item1", "Desc", 100.0, userId));
        productRepository.save(new Product("Item2", "Desc", 200.0, userId));

        long count = productRepository.countByUserId(userId);

        assertEquals(3, count);
    }

    @Test
    void testDeleteByUserId() {
        productRepository.save(new Product("Item1", "Desc", 100.0, userId));

        productRepository.deleteByUserId(userId);

        List<Product> products = productRepository.findByUserId(userId);
        assertEquals(0, products.size());
    }

    @Test
    void testExistsByNameAndUserId() {
        assertTrue(productRepository.existsByNameAndUserId("Laptop", userId));
        assertFalse(productRepository.existsByNameAndUserId("NonExistent", userId));
        assertFalse(productRepository.existsByNameAndUserId("Laptop", "wrongUser"));
    }

    @Test
    void testFindByPriceGreaterThan() {
        productRepository.save(new Product("Cheap", "Item", 50.0, userId));

        List<Product> expensive = productRepository.findByPriceGreaterThan(100.0);

        assertEquals(1, expensive.size());
        assertEquals("Laptop", expensive.get(0).getName());
    }

    @Test
    void testFindByPriceLessThan() {
        productRepository.save(new Product("Cheap", "Item", 50.0, userId));

        List<Product> cheap = productRepository.findByPriceLessThan(100.0);

        assertEquals(1, cheap.size());
        assertEquals("Cheap", cheap.get(0).getName());
    }
}
