package com.letsplay.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.letsplay.api.dto.CreateProductRequest;
import com.letsplay.api.dto.ProductDTO;
import com.letsplay.api.dto.UpdateProductRequest;
import com.letsplay.api.exception.ForbiddenException;
import com.letsplay.api.exception.ResourceNotFoundException;
import com.letsplay.api.ratelimit.RateLimitFilter;
import com.letsplay.api.ratelimit.RateLimitService;
import com.letsplay.api.security.JwtAuthenticationEntryPoint;
import com.letsplay.api.security.JwtAuthenticationFilter;
import com.letsplay.api.security.JwtUtil;
import com.letsplay.api.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ProductController
 */
@WebMvcTest(ProductController.class)
@Import(com.letsplay.api.config.SecurityConfig.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private RateLimitService rateLimitService;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private CorsConfigurationSource corsConfigurationSource;

    private ProductDTO productDTO;
    private ProductDTO productDTO2;
    private CreateProductRequest createRequest;
    private UpdateProductRequest updateRequest;

    @BeforeEach
    void setUp() {
        productDTO = new ProductDTO();
        productDTO.setId("product123");
        productDTO.setName("Test Product");
        productDTO.setDescription("Test Description");
        productDTO.setPrice(99.99);
        productDTO.setUserId("user123");

        productDTO2 = new ProductDTO();
        productDTO2.setId("product456");
        productDTO2.setName("Product 2");
        productDTO2.setDescription("Description 2");
        productDTO2.setPrice(149.99);
        productDTO2.setUserId("user456");

        createRequest = new CreateProductRequest();
        createRequest.setName("New Product");
        createRequest.setDescription("New Description");
        createRequest.setPrice(49.99);

        updateRequest = new UpdateProductRequest();
        updateRequest.setName("Updated Product");
        updateRequest.setPrice(199.99);
    }

    @Test
    void testGetAllProductsWithoutAuth() throws Exception {
        // Setup - GET /products is PUBLIC
        List<ProductDTO> products = Arrays.asList(productDTO, productDTO2);
        when(productService.getAllProducts()).thenReturn(products);

        // Execute & Verify
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("product123"))
                .andExpect(jsonPath("$[1].id").value("product456"));
    }

    @Test
    @WithMockUser(username = "john@example.com", roles = "USER")
    void testGetAllProductsWithAuth() throws Exception {
        // Setup - Should also work with authentication
        List<ProductDTO> products = Arrays.asList(productDTO);
        when(productService.getAllProducts()).thenReturn(products);

        // Execute & Verify
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testGetProductByIdWithoutAuth() throws Exception {
        // Setup - GET /products/{id} is PUBLIC
        when(productService.getProductById("product123")).thenReturn(productDTO);

        // Execute & Verify
        mockMvc.perform(get("/api/products/product123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("product123"))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(99.99));
    }

    @Test
    void testGetProductByIdNotFound() throws Exception {
        // Setup
        when(productService.getProductById("nonexistent"))
                .thenThrow(new ResourceNotFoundException("Product not found"));

        // Execute & Verify
        mockMvc.perform(get("/api/products/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(username = "john@example.com", roles = "USER")
    void testGetProductsByUserId() throws Exception {
        // Setup
        List<ProductDTO> products = Arrays.asList(productDTO);
        when(productService.getProductsByUserId("user123")).thenReturn(products);

        // Execute & Verify
        mockMvc.perform(get("/api/products/user/user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].userId").value("user123"));
    }

    @Test
    @WithMockUser(username = "john@example.com", roles = "USER")
    void testCreateProduct() throws Exception {
        // Setup
        ProductDTO createdProduct = new ProductDTO();
        createdProduct.setId("newProduct123");
        createdProduct.setName("New Product");
        createdProduct.setDescription("New Description");
        createdProduct.setPrice(49.99);
        createdProduct.setUserId("user123");

        when(productService.createProduct(any(CreateProductRequest.class)))
                .thenReturn(createdProduct);

        // Execute & Verify
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("newProduct123"))
                .andExpect(jsonPath("$.name").value("New Product"))
                .andExpect(jsonPath("$.price").value(49.99));
    }

    @Test
    void testCreateProductWithoutAuth() throws Exception {
        // Execute & Verify - Should require authentication
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "john@example.com", roles = "USER")
    void testCreateProductWithInvalidData() throws Exception {
        // Setup - Missing required name
        createRequest.setName(null);

        // Execute & Verify
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "john@example.com", roles = "USER")
    void testCreateProductWithNegativePrice() throws Exception {
        // Setup
        createRequest.setPrice(-10.0);

        // Execute & Verify
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "john@example.com", roles = "USER")
    void testUpdateProductAsOwner() throws Exception {
        // Setup
        ProductDTO updatedProduct = new ProductDTO();
        updatedProduct.setId("product123");
        updatedProduct.setName("Updated Product");
        updatedProduct.setPrice(199.99);
        updatedProduct.setUserId("user123");

        when(productService.updateProduct(eq("product123"), any(UpdateProductRequest.class)))
                .thenReturn(updatedProduct);

        // Execute & Verify
        mockMvc.perform(put("/api/products/product123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Product"))
                .andExpect(jsonPath("$.price").value(199.99));
    }

    @Test
    @WithMockUser(username = "john@example.com", roles = "USER")
    void testUpdateOtherUserProductAsUser() throws Exception {
        // Setup - User trying to update another user's product
        when(productService.updateProduct(eq("product456"), any(UpdateProductRequest.class)))
                .thenThrow(new ForbiddenException("You do not have permission to modify this product"));

        // Execute & Verify
        mockMvc.perform(put("/api/products/product456")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void testUpdateProductAsAdmin() throws Exception {
        // Setup - Admin can update any product
        ProductDTO updatedProduct = new ProductDTO();
        updatedProduct.setId("product123");
        updatedProduct.setName("Updated by Admin");
        updatedProduct.setPrice(199.99);
        updatedProduct.setUserId("user123");

        when(productService.updateProduct(eq("product123"), any(UpdateProductRequest.class)))
                .thenReturn(updatedProduct);

        // Execute & Verify
        mockMvc.perform(put("/api/products/product123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateProductWithoutAuth() throws Exception {
        // Execute & Verify
        mockMvc.perform(put("/api/products/product123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "john@example.com", roles = "USER")
    void testDeleteProductAsOwner() throws Exception {
        // Setup
        doNothing().when(productService).deleteProduct("product123");

        // Execute & Verify
        mockMvc.perform(delete("/api/products/product123"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "john@example.com", roles = "USER")
    void testDeleteOtherUserProductAsUser() throws Exception {
        // Setup
        doThrow(new ForbiddenException("You do not have permission to modify this product"))
                .when(productService).deleteProduct("product456");

        // Execute & Verify
        mockMvc.perform(delete("/api/products/product456"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void testDeleteProductAsAdmin() throws Exception {
        // Setup - Admin can delete any product
        doNothing().when(productService).deleteProduct("product123");

        // Execute & Verify
        mockMvc.perform(delete("/api/products/product123"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteProductWithoutAuth() throws Exception {
        // Execute & Verify
        mockMvc.perform(delete("/api/products/product123"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "john@example.com", roles = "USER")
    void testDeleteProductNotFound() throws Exception {
        // Setup
        doThrow(new ResourceNotFoundException("Product not found"))
                .when(productService).deleteProduct("nonexistent");

        // Execute & Verify
        mockMvc.perform(delete("/api/products/nonexistent"))
                .andExpect(status().isNotFound());
    }
}
