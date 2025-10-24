package com.letsplay.api.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void testProductCreation() {
        Product product = new Product("Laptop", "Gaming laptop", 1299.99, "user123");

        assertNotNull(product);
        assertEquals("Laptop", product.getName());
        assertEquals("Gaming laptop", product.getDescription());
        assertEquals(1299.99, product.getPrice());
        assertEquals("user123", product.getUserId());
        assertNotNull(product.getCreatedAt());
        assertNotNull(product.getUpdatedAt());
    }

    @Test
    void testPriceValidation() {
        Product product = new Product("Item", "Description", 99.99, "user123");

        assertTrue(ModelValidator.isValidPrice(product.getPrice()));

        product.setPrice(-10.0);
        assertFalse(ModelValidator.isValidPrice(product.getPrice()));
    }
}
