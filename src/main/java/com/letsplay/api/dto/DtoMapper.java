package com.letsplay.api.dto;

import com.letsplay.api.model.Product;
import com.letsplay.api.model.User;

public class DtoMapper {

    /**
     * Convert User entity to UserDTO (excludes password)
     */
    public static UserDTO toUserDTO(User user) {
        if (user == null) {
            return null;
        }

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        return dto;
    }

    /**
     * Convert Product entity to ProductDTO
     */
    public static ProductDTO toProductDTO(Product product) {
        if (product == null) {
            return null;
        }

        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setUserId(product.getUserId());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());

        return dto;
    }

    /**
     * Convert Product entity to ProductDTO with owner details
     */
    public static ProductDTO toProductDTOWithOwner(Product product, User owner) {
        ProductDTO dto = toProductDTO(product);
        if (dto != null && owner != null) {
            dto.setOwner(toUserDTO(owner));
        }
        return dto;
    }

    /**
     * Update User entity from UpdateUserRequest
     */
    public static void updateUserFromRequest(User user, UpdateUserRequest request) {
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        // Password is handled separately with hashing
    }

    /**
     * Update Product entity from UpdateProductRequest
     */
    public static void updateProductFromRequest(Product product, UpdateProductRequest request) {
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
    }

    /**
     * Create Product entity from CreateProductRequest
     */
    public static Product toProduct(CreateProductRequest request, String userId) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setUserId(userId);
        return product;
    }

    /**
     * Create User entity from RegisterRequest
     */
    public static User toUser(RegisterRequest request, String hashedPassword) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(hashedPassword);
        user.setRole(request.getRole());
        return user;
    }

    private DtoMapper() {
        // Private constructor to prevent instantiation
    }
}
