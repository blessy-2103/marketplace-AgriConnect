package com.agriconnect.controller;

import com.agriconnect.dto.ProductRequest;
import com.agriconnect.model.OrderItem;
import com.agriconnect.model.Product;
import com.agriconnect.model.User;
import com.agriconnect.service.ProductService;
import com.agriconnect.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farmer")
public class FarmerController {

    private final ProductService productService;
    private final OrderService orderService;

    public FarmerController(ProductService productService, OrderService orderService) {
        this.productService = productService;
        this.orderService = orderService;
    }

    @GetMapping("/products")
    public ResponseEntity<List<Product>> myProducts(@AuthenticationPrincipal User farmer) {
        return ResponseEntity.ok(productService.getByFarmer(farmer));
    }

    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductRequest request,
                                                  @AuthenticationPrincipal User farmer) {
        return ResponseEntity.ok(productService.create(request, farmer));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id,
                                                  @Valid @RequestBody ProductRequest request,
                                                  @AuthenticationPrincipal User farmer) {
        return ResponseEntity.ok(productService.update(id, request, farmer));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id, @AuthenticationPrincipal User farmer) {
        productService.delete(id, farmer);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderItem>> incomingOrders(@AuthenticationPrincipal User farmer) {
        return ResponseEntity.ok(orderService.getOrderItemsForFarmer(farmer));
    }
}
