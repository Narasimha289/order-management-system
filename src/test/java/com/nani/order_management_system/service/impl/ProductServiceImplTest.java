
package com.nani.order_management_system.service.impl;

import com.nani.order_management_system.entity.Product;
import com.nani.order_management_system.repository.ProductRepository;
import com.nani.order_management_system.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Wireless Mouse");
        product.setDescription("Wireless mouse with USB receiver");
        product.setPrice(new BigDecimal("799.00"));
        product.setQuantity(25);
    }

    @Test
    void createProductShouldReturnSavedProduct() {
        when(productRepository.save(any(Product.class)))
                .thenReturn(product);

        Product result = productService.createProduct(product);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Wireless Mouse", result.getName());
        verify(productRepository).save(product);
    }

    @Test
    void getAllProductsShouldReturnProductList() {
        when(productRepository.findAll()).thenReturn(List.of(product));

        List<Product> result = productService.getAllProducts();

        assertEquals(1, result.size());
        assertEquals("Wireless Mouse", result.get(0).getName());
        verify(productRepository).findAll();
    }

    @Test
    void getProductByIdShouldReturnProductWhenFound() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        Product result = productService.getProductById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Wireless Mouse", result.getName());
    }

    @Test
    void getProductByIdShouldThrowWhenProductNotFound() {
        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.getProductById(99L)
        );
    }

    @Test
    void updateProductShouldReturnUpdatedProduct() {
        Product updatedProduct = new Product();
        updatedProduct.setName("Gaming Mouse");
        updatedProduct.setDescription("RGB gaming mouse");
        updatedProduct.setPrice(new BigDecimal("1299.00"));
        updatedProduct.setQuantity(15);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.updateProduct(1L, updatedProduct);

        assertEquals("Gaming Mouse", result.getName());
        assertEquals(new BigDecimal("1299.00"), result.getPrice());
        assertEquals(15, result.getQuantity());
        verify(productRepository).save(product);
    }


    @Test
    void deleteProductShouldDeleteWhenProductExists() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        productService.deleteProduct(1L);

        verify(productRepository).findById(1L);
        verify(productRepository).delete(product);
    }

}
