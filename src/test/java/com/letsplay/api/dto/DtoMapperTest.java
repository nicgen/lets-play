package com.letsplay.api.dto;

import com.letsplay.api.model.Product;
import com.letsplay.api.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DtoMapperTest {

    @Test
    void testToUserDTO_excludesPassword() {
        User user = new User("John Doe", "john@example.com", "password123", "ROLE_USER");
        user.setId("123");

        UserDTO dto = DtoMapper.toUserDTO(user);

        assertNotNull(dto);
        assertEquals("123", dto.getId());
        assertEquals("John Doe", dto.getName());
        assertEquals("john@example.com", dto.getEmail());
        assertEquals("ROLE_USER", dto.getRole());
        // Password should not be accessible in DTO
    }

    @Test
    void testToUserDTO_null() {
        UserDTO dto = DtoMapper.toUserDTO(null);
        assertNull(dto);
    }

    @Test
    void testToProductDTO() {
        Product product = new Product("Laptop", "Gaming laptop", 1299.99, "user123");
        product.setId("456");

        ProductDTO dto = DtoMapper.toProductDTO(product);

        assertNotNull(dto);
        assertEquals("456", dto.getId());
        assertEquals("Laptop", dto.getName());
        assertEquals("Gaming laptop", dto.getDescription());
        assertEquals(1299.99, dto.getPrice());
        assertEquals("user123", dto.getUserId());
    }

    @Test
    void testToProductDTOWithOwner() {
        User owner = new User("John", "john@example.com", "pass", "ROLE_USER");
        owner.setId("user123");

        Product product = new Product("Laptop", "Gaming laptop", 1299.99, "user123");
        product.setId("456");

        ProductDTO dto = DtoMapper.toProductDTOWithOwner(product, owner);

        assertNotNull(dto);
        assertNotNull(dto.getOwner());
        assertEquals("John", dto.getOwner().getName());
    }

    @Test
    void testUpdateUserFromRequest() {
        User user = new User("Old Name", "old@example.com", "pass", "ROLE_USER");
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("New Name");
        request.setEmail("new@example.com");

        DtoMapper.updateUserFromRequest(user, request);

        assertEquals("New Name", user.getName());
        assertEquals("new@example.com", user.getEmail());
    }

    @Test
    void testUpdateProductFromRequest() {
        Product product = new Product("Old", "Old desc", 100.0, "user123");
        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("New");
        request.setPrice(200.0);

        DtoMapper.updateProductFromRequest(product, request);

        assertEquals("New", product.getName());
        assertEquals(200.0, product.getPrice());
        assertEquals("Old desc", product.getDescription()); // Unchanged
    }

    @Test
    void testToProduct() {
        CreateProductRequest request = new CreateProductRequest("Laptop", "Gaming", 1299.99);

        Product product = DtoMapper.toProduct(request, "user123");

        assertNotNull(product);
        assertEquals("Laptop", product.getName());
        assertEquals("Gaming", product.getDescription());
        assertEquals(1299.99, product.getPrice());
        assertEquals("user123", product.getUserId());
    }
}
