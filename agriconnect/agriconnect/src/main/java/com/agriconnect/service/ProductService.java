package com.agriconnect.service;

import com.agriconnect.dto.ProductRequest;
import com.agriconnect.exception.BadRequestException;
import com.agriconnect.exception.ResourceNotFoundException;
import com.agriconnect.model.Category;
import com.agriconnect.model.Product;
import com.agriconnect.model.Role;
import com.agriconnect.model.User;
import com.agriconnect.repository.CategoryRepository;
import com.agriconnect.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Product> getAllActive() {
        return productRepository.findByActiveTrue();
    }

    public List<Product> getByCategory(Long categoryId) {
        return productRepository.findByCategoryIdAndActiveTrue(categoryId);
    }

    public List<Product> search(String keyword) {
        return productRepository.search(keyword);
    }

    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));
    }

    public List<Product> getByFarmer(User farmer) {
        return productRepository.findByFarmer(farmer);
    }

    @Transactional
    public Product create(ProductRequest request, User farmer) {
        if (farmer.getRole() != Role.FARMER) {
            throw new BadRequestException("Only farmers can list products");
        }
        Product product = new Product();
        applyRequest(product, request);
        product.setFarmer(farmer);
        return productRepository.save(product);
    }

    @Transactional
    public Product update(Long id, ProductRequest request, User farmer) {
        Product product = getById(id);
        ensureOwnership(product, farmer);
        applyRequest(product, request);
        return productRepository.save(product);
    }

    @Transactional
    public void delete(Long id, User farmer) {
        Product product = getById(id);
        ensureOwnership(product, farmer);
        product.setActive(false);
        productRepository.save(product);
    }

    private void applyRequest(Product product, ProductRequest request) {
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setUnit(request.getUnit());
        product.setQuantityAvailable(request.getQuantityAvailable());
        product.setImageUrl(request.getImageUrl());
        product.setOrganic(request.isOrganic());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            product.setCategory(category);
        }
    }

    private void ensureOwnership(Product product, User farmer) {
        if (!product.getFarmer().getId().equals(farmer.getId())) {
            throw new BadRequestException("You do not have permission to modify this product");
        }
    }
}
