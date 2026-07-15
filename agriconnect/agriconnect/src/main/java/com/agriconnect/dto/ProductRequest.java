package com.agriconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than zero")
    private BigDecimal price;

    @NotBlank(message = "Unit is required, e.g. kg, dozen, litre")
    private String unit;

    @NotNull(message = "Quantity available is required")
    @PositiveOrZero(message = "Quantity cannot be negative")
    private Integer quantityAvailable;

    private String imageUrl;

    private boolean organic;

    private Long categoryId;
}
