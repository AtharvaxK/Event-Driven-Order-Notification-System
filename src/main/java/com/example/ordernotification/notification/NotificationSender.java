package com.example.ordernotification.notification;

public interface NotificationSender {
    void send(String to, String subject, String body);
}
