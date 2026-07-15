package com.agriconnect.controller;

import com.agriconnect.dto.OrderRequest;
import com.agriconnect.model.Order;
import com.agriconnect.model.OrderStatus;
import com.agriconnect.model.User;
import com.agriconnect.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> placeOrder(@Valid @RequestBody OrderRequest request,
                                             @AuthenticationPrincipal User customer) {
        return ResponseEntity.ok(orderService.placeOrder(request, customer));
    }

    @GetMapping
    public ResponseEntity<List<Order>> myOrders(@AuthenticationPrincipal User customer) {
        return ResponseEntity.ok(orderService.getOrdersForCustomer(customer));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id, @AuthenticationPrincipal User requester) {
        return ResponseEntity.ok(orderService.getByIdForUser(id, requester));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable Long id,
                                               @RequestBody Map<String, String> body,
                                               @AuthenticationPrincipal User requester) {
        OrderStatus status = OrderStatus.valueOf(body.get("status").toUpperCase());
        return ResponseEntity.ok(orderService.updateStatus(id, status, requester));
    }
}
