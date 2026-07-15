package com.agriconnect.repository;

import com.agriconnect.model.OrderItem;
import com.agriconnect.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByFarmerOrderByIdDesc(User farmer);
}
