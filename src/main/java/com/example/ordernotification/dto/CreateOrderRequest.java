package com.example.ordernotification.dto;

import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateOrderRequest(
        @Positive(message = "User ID must be positive")
        Long userId,

        @Positive(message = "Total amount must be positive")
        BigDecimal totalAmount
) {}
