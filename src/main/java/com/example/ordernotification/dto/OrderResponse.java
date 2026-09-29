package com.example.ordernotification.dto;

import com.example.ordernotification.entity.OrderStatus;

import java.math.BigDecimal;

public record OrderResponse(
        Long id,
        Long userId,
        BigDecimal totalAmount,
        OrderStatus status
) {}
