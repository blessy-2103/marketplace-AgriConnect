package com.agriconnect.service;

import com.agriconnect.dto.OrderRequest;
import com.agriconnect.exception.BadRequestException;
import com.agriconnect.exception.ResourceNotFoundException;
import com.agriconnect.model.*;
import com.agriconnect.repository.OrderItemRepository;
import com.agriconnect.repository.OrderRepository;
import com.agriconnect.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository,
                         OrderItemRepository orderItemRepository,
                         ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Order placeOrder(OrderRequest request, User customer) {
        if (customer.getRole() != Role.CUSTOMER) {
            throw new BadRequestException("Only customers can place orders");
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setContactPhone(request.getContactPhone());

        BigDecimal total = BigDecimal.ZERO;

        for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemReq.getProductId()));

            if (!product.isActive()) {
                throw new BadRequestException(product.getName() + " is no longer available");
            }
            if (itemReq.getQuantity() == null || itemReq.getQuantity() <= 0) {
                throw new BadRequestException("Invalid quantity for " + product.getName());
            }
            if (product.getQuantityAvailable() < itemReq.getQuantity()) {
                throw new BadRequestException("Not enough stock for " + product.getName() +
                        ". Available: " + product.getQuantityAvailable() + " " + product.getUnit());
            }

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setFarmer(product.getFarmer());
            item.setQuantity(itemReq.getQuantity());
            item.setPriceAtOrder(product.getPrice());
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            item.setSubtotal(subtotal);

            order.addItem(item);
            total = total.add(subtotal);

            // Deduct stock
            product.setQuantityAvailable(product.getQuantityAvailable() - itemReq.getQuantity());
            productRepository.save(product);
        }

        order.setTotalAmount(total);
        return orderRepository.save(order);
    }

    public List<Order> getOrdersForCustomer(User customer) {
        return orderRepository.findByCustomerOrderByOrderDateDesc(customer);
    }

    public Order getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + id));
    }

    public Order getByIdForUser(Long id, User requester) {
        Order order = getById(id);
        boolean isOwner = order.getCustomer().getId().equals(requester.getId());
        boolean isFulfillingFarmer = order.getItems().stream()
                .anyMatch(i -> i.getFarmer().getId().equals(requester.getId()));
        if (!isOwner && !isFulfillingFarmer && requester.getRole() != Role.ADMIN) {
            throw new BadRequestException("You do not have permission to view this order");
        }
        return order;
    }

    public List<OrderItem> getOrderItemsForFarmer(User farmer) {
        return orderItemRepository.findByFarmerOrderByIdDesc(farmer);
    }

    @Transactional
    public Order updateStatus(Long orderId, OrderStatus status, User requester) {
        Order order = getById(orderId);

        boolean isOwner = order.getCustomer().getId().equals(requester.getId());
        boolean isFulfillingFarmer = order.getItems().stream()
                .anyMatch(i -> i.getFarmer().getId().equals(requester.getId()));

        if (!isOwner && !isFulfillingFarmer && requester.getRole() != Role.ADMIN) {
            throw new BadRequestException("You do not have permission to update this order");
        }

        order.setStatus(status);
        return orderRepository.save(order);
    }
}
