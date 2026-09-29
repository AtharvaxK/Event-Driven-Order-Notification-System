package com.example.ordernotification.kafka.consumer;

import com.example.ordernotification.entity.Notification;
import com.example.ordernotification.entity.NotificationStatus;
import com.example.ordernotification.entity.ProcessedEvent;
import com.example.ordernotification.kafka.event.OrderCreatedEvent;
import com.example.ordernotification.notification.NotificationSender;
import com.example.ordernotification.repository.NotificationRepository;
import com.example.ordernotification.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final NotificationRepository notificationRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final NotificationSender notificationSender;

    public OrderEventConsumer(NotificationRepository notificationRepository,
                              ProcessedEventRepository processedEventRepository,
                              NotificationSender notificationSender) {
        this.notificationRepository = notificationRepository;
        this.processedEventRepository = processedEventRepository;
        this.notificationSender = notificationSender;
    }

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 2000, multiplier = 2.0),
            dltTopicSuffix = "-dlt",
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            dltStrategy = org.springframework.kafka.retrytopic.DltStrategy.FAIL_ON_ERROR
    )
    @KafkaListener(topics = "${app.kafka.topics.order-events}", groupId = "notification-service")
    @Transactional
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent: eventId={}, orderId={}", event.getEventId(), event.getOrderId());

        // Idempotency check
        if (processedEventRepository.existsByEventId(event.getEventId())) {
            log.warn("Duplicate event received, skipping: eventId={}", event.getEventId());
            return;
        }

        // Create PENDING notification
        String subject = "Order #" + event.getOrderId() + " Created";
        String message = buildEmailBody(event);

        Notification notification = new Notification(
                event.getEventId(),
                event.getOrderId(),
                event.getUserEmail(),
                subject,
                message
        );
        notificationRepository.save(notification);
        log.info("Processing notification: orderId={}", event.getOrderId());

        // Send email (or mock)
        notificationSender.send(event.getUserEmail(), subject, message);

        // Mark notification as SENT
        notification.setStatus(NotificationStatus.SENT);
        notificationRepository.save(notification);
        log.info("Notification marked SENT: orderId={}", event.getOrderId());

        // Mark event as processed (idempotency)
        processedEventRepository.save(new ProcessedEvent(event.getEventId()));
        log.info("Event marked as processed: eventId={}", event.getEventId());
    }

    private String buildEmailBody(OrderCreatedEvent event) {
        return String.format("""
                Hello,

                Your order #%d has been created successfully.

                Order Amount: ₹%.2f
                Status: CREATED

                Thank you for your order!
                """,
                event.getOrderId(),
                event.getOrderAmount());
    }
}
