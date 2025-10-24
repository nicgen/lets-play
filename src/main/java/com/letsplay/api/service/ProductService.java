package com.letsplay.api.service;

import com.letsplay.api.dto.*;
import com.letsplay.api.exception.ForbiddenException;
import com.letsplay.api.exception.ResourceNotFoundException;
import com.letsplay.api.model.Product;
import com.letsplay.api.repository.ProductRepository;
import com.letsplay.api.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(DtoMapper::toProductDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return DtoMapper.toProductDTO(product);
    }

    public List<ProductDTO> getProductsByUserId(String userId) {
        return productRepository.findByUserId(userId).stream()
                .map(DtoMapper::toProductDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO createProduct(CreateProductRequest request) {
        String currentUserId = getCurrentUserId();

        Product product = DtoMapper.toProduct(request, currentUserId);
        product = productRepository.save(product);

        return DtoMapper.toProductDTO(product);
    }

    public ProductDTO updateProduct(String id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // Check ownership
        checkProductOwnership(product);

        DtoMapper.updateProductFromRequest(product, request);
        product = productRepository.save(product);

        return DtoMapper.toProductDTO(product);
    }

    public void deleteProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // Check ownership
        checkProductOwnership(product);

        productRepository.deleteById(id);
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException("User not authenticated");
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getId();
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null &&
               authentication.getAuthorities().stream()
                       .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private void checkProductOwnership(Product product) {
        if (!isAdmin() && !product.getUserId().equals(getCurrentUserId())) {
            throw new ForbiddenException("You do not have permission to modify this product");
        }
    }
}
