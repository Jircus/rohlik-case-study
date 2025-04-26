package com.rohlikgroup.casestudy.service;


import com.rohlikgroup.casestudy.dto.ProductDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface ProductService {

    /**
     * Creates a new product.
     *
     * @param product the product to create
     * @return the created product
     */
    ProductDto createProduct(@NotNull @Valid ProductDto product);

    /**
     * Deletes a product by its ID.
     *
     * @param productId the ID of the product to delete
     */
    void deleteProduct(@NotNull Long productId);

    /**
     * Updates a product.
     *
     * @param productId      the ID of the product to update
     * @param updatedProduct the updated product
     * @return the updated product
     */
    ProductDto updateProduct(@NotNull Long productId, @NotNull @Valid ProductDto updatedProduct);

    /**
     * Retrieves all products.
     *
     * @return a list of all products
     */
    List<ProductDto> getProducts();

    /**
     * Retrieves a product by its ID.
     *
     * @param productId the ID of the product to retrieve
     * @return the product with the specified ID
     */
    ProductDto getProduct(@NotNull Long productId);

}
