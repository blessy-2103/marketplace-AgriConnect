package com.agriconnect.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderRequest {

    @NotBlank(message = "Delivery address is required")
    private String deliveryAddress;

    private String contactPhone;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequest> items;

    @Getter
    @Setter
    public static class OrderItemRequest {
        @NotNull(message = "productId is required for each item")
        private Long productId;

        @NotNull(message = "quantity is required for each item")
        @Positive(message = "quantity must be greater than zero")
        private Integer quantity;
    }
}
