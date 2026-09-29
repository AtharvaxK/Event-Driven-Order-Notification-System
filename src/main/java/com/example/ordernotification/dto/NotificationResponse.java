package com.example.ordernotification.dto;

import com.example.ordernotification.entity.NotificationStatus;

public record NotificationResponse(
        Long id,
        String eventId,
        Long orderId,
        String recipientEmail,
        String subject,
        NotificationStatus status,
        int retryCount
) {}
