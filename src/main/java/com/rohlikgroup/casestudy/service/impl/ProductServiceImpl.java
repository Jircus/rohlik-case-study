package com.rohlikgroup.casestudy.service.impl;

import com.rohlikgroup.casestudy.dto.ProductDto;
import com.rohlikgroup.casestudy.entity.OrderStatus;
import com.rohlikgroup.casestudy.entity.Product;
import com.rohlikgroup.casestudy.mapper.ProductMapper;
import com.rohlikgroup.casestudy.repository.OrderRepository;
import com.rohlikgroup.casestudy.repository.ProductRepository;
import com.rohlikgroup.casestudy.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductDto createProduct(ProductDto product) {

        var newProductEntity = productMapper.map(product);
        return productMapper.map(productRepository.save(newProductEntity));
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId) {
        boolean isProductInActiveOrders = orderRepository.existsByOrderItemsProductIdAndStatusNotIn(productId, OrderStatus.getInactiveStatuses());
        if (isProductInActiveOrders) {
            throw new IllegalStateException("Cannot delete product that is part of an active order.");
        }
        productRepository.deleteById(productId);
    }

    @Override
    @Transactional
    public ProductDto updateProduct(Long productId, ProductDto updatedProduct) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + productId));

        existingProduct.setName(updatedProduct.name());
        existingProduct.setPrice(updatedProduct.price());
        existingProduct.setStockAmount(updatedProduct.stockAmount());

        return productMapper.map(productRepository.save(existingProduct));
    }

    @Override
    public List<ProductDto> getProducts() {
        return productRepository.findAll().stream().map(productMapper::map).toList();
    }

    @Override
    public ProductDto getProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + productId));
        return productMapper.map(product);
    }
}

