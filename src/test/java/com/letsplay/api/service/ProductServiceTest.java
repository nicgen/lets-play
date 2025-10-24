package com.letsplay.api.service;

import com.letsplay.api.dto.CreateProductRequest;
import com.letsplay.api.dto.ProductDTO;
import com.letsplay.api.dto.UpdateProductRequest;
import com.letsplay.api.exception.ForbiddenException;
import com.letsplay.api.exception.ResourceNotFoundException;
import com.letsplay.api.model.Product;
import com.letsplay.api.model.Role;
import com.letsplay.api.model.User;
import com.letsplay.api.repository.ProductRepository;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;

/**
 * Unit tests for ProductService
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private User user;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId("user123");
        user.setEmail("john@example.com");
        user.setRoleEnum(Role.USER);

        userDetails = new CustomUserDetails(user);

        product = new Product();
        product.setId("product123");
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(99.99);
        product.setUserId("user123");

        // Setup security context
        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.isAuthenticated()).thenReturn(true);
        lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
    }

    @Test
    void testGetAllProducts() {
        // Setup
        Product product2 = new Product();
        product2.setId("product456");
        product2.setName("Product 2");
        product2.setUserId("user456");

        when(productRepository.findAll()).thenReturn(Arrays.asList(product, product2));

        // Execute
        List<ProductDTO> products = productService.getAllProducts();

        // Verify
        assertNotNull(products);
        assertEquals(2, products.size());
        assertEquals("Test Product", products.get(0).getName());
        assertEquals("Product 2", products.get(1).getName());
        verify(productRepository).findAll();
    }

    @Test
    void testGetProductById() {
        // Setup
        when(productRepository.findById("product123")).thenReturn(Optional.of(product));

        // Execute
        ProductDTO result = productService.getProductById("product123");

        // Verify
        assertNotNull(result);
        assertEquals("product123", result.getId());
        assertEquals("Test Product", result.getName());
        verify(productRepository).findById("product123");
    }

    @Test
    void testGetProductByIdNotFound() {
        // Setup
        when(productRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // Execute & Verify
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> productService.getProductById("nonexistent")
        );

        assertTrue(exception.getMessage().contains("Product not found"));
        verify(productRepository).findById("nonexistent");
    }

    @Test
    void testGetProductsByUserId() {
        // Setup
        when(productRepository.findByUserId("user123")).thenReturn(Arrays.asList(product));

        // Execute
        List<ProductDTO> products = productService.getProductsByUserId("user123");

        // Verify
        assertNotNull(products);
        assertEquals(1, products.size());
        assertEquals("Test Product", products.get(0).getName());
        verify(productRepository).findByUserId("user123");
    }

    @Test
    void testCreateProduct() {
        // Setup
        CreateProductRequest request = new CreateProductRequest();
        request.setName("New Product");
        request.setDescription("New Description");
        request.setPrice(49.99);

        Product savedProduct = new Product();
        savedProduct.setId("newProduct123");
        savedProduct.setName(request.getName());
        savedProduct.setDescription(request.getDescription());
        savedProduct.setPrice(request.getPrice());
        savedProduct.setUserId("user123");

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // Execute
        ProductDTO result = productService.createProduct(request);

        // Verify
        assertNotNull(result);
        assertEquals("New Product", result.getName());
        assertEquals("user123", result.getUserId());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void testUpdateProductAsOwner() {
        // Setup
        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("Updated Product");
        request.setPrice(149.99);

        when(productRepository.findById("product123")).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Execute
        ProductDTO result = productService.updateProduct("product123", request);

        // Verify
        assertNotNull(result);
        assertEquals("Updated Product", result.getName());
        verify(productRepository).findById("product123");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void testUpdateProductAsAdmin() {
        // Setup - Make current user an admin
        User adminUser = new User();
        adminUser.setId("admin123");
        adminUser.setRoleEnum(Role.ADMIN);
        CustomUserDetails adminDetails = new CustomUserDetails(adminUser);

        lenient().when(authentication.getPrincipal()).thenReturn(adminDetails);
        when(authentication.getAuthorities()).thenAnswer(invocation ->
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));

        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("Admin Updated");

        when(productRepository.findById("product123")).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Execute
        ProductDTO result = productService.updateProduct("product123", request);

        // Verify
        assertNotNull(result);
        verify(productRepository).findById("product123");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void testUpdateProductUnauthorized() {
        // Setup - Product owned by different user
        product.setUserId("otherUser123");

        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("Unauthorized Update");

        when(productRepository.findById("product123")).thenReturn(Optional.of(product));
        when(authentication.getAuthorities()).thenAnswer(invocation ->
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        // Execute & Verify
        ForbiddenException exception = assertThrows(
            ForbiddenException.class,
            () -> productService.updateProduct("product123", request)
        );

        assertTrue(exception.getMessage().contains("permission"));
        verify(productRepository).findById("product123");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testDeleteProductAsOwner() {
        // Setup
        when(productRepository.findById("product123")).thenReturn(Optional.of(product));
        doNothing().when(productRepository).deleteById("product123");

        // Execute
        productService.deleteProduct("product123");

        // Verify
        verify(productRepository).findById("product123");
        verify(productRepository).deleteById("product123");
    }

    @Test
    void testDeleteProductUnauthorized() {
        // Setup - Product owned by different user
        product.setUserId("otherUser123");

        when(productRepository.findById("product123")).thenReturn(Optional.of(product));
        when(authentication.getAuthorities()).thenAnswer(invocation ->
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        // Execute & Verify
        ForbiddenException exception = assertThrows(
            ForbiddenException.class,
            () -> productService.deleteProduct("product123")
        );

        assertTrue(exception.getMessage().contains("permission"));
        verify(productRepository).findById("product123");
        verify(productRepository, never()).deleteById(anyString());
    }

    @Test
    void testDeleteProductNotFound() {
        // Setup
        when(productRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // Execute & Verify
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> productService.deleteProduct("nonexistent")
        );

        assertTrue(exception.getMessage().contains("Product not found"));
        verify(productRepository).findById("nonexistent");
        verify(productRepository, never()).deleteById(anyString());
    }

    @Test
    void testCreateProductAutoAssignsCurrentUser() {
        // Setup
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Auto Assign Test");
        request.setDescription("Testing user assignment");
        request.setPrice(29.99);

        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product saved = invocation.getArgument(0);
            assertEquals("user123", saved.getUserId());
            saved.setId("assigned123");
            return saved;
        });

        // Execute
        ProductDTO result = productService.createProduct(request);

        // Verify
        assertNotNull(result);
        assertEquals("user123", result.getUserId());
        verify(productRepository).save(any(Product.class));
    }
}
