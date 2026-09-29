package com.example.ordernotification.kafka.event;

import java.math.BigDecimal;

public class OrderCreatedEvent {

    private String eventId;
    private String eventType;
    private Long orderId;
    private Long userId;
    private String userEmail;
    private BigDecimal orderAmount;

    public OrderCreatedEvent() {}

    public OrderCreatedEvent(String eventId, Long orderId, Long userId, String userEmail, BigDecimal orderAmount) {
        this.eventId = eventId;
        this.eventType = "ORDER_CREATED";
        this.orderId = orderId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.orderAmount = orderAmount;
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public BigDecimal getOrderAmount() { return orderAmount; }
    public void setOrderAmount(BigDecimal orderAmount) { this.orderAmount = orderAmount; }

    @Override
    public String toString() {
        return "OrderCreatedEvent{eventId='" + eventId + "', eventType='" + eventType +
               "', orderId=" + orderId + ", userId=" + userId + ", userEmail='" + userEmail +
               "', orderAmount=" + orderAmount + "}";
    }
}
