package com.example.ordernotification.repository;

import com.example.ordernotification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByOrderId(Long orderId);
    Optional<Notification> findByEventId(String eventId);
}
