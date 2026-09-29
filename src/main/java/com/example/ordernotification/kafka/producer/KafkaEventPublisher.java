package com.example.ordernotification.kafka.producer;

import com.example.ordernotification.kafka.event.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class KafkaEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String orderEventsTopic;

    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                               @Value("${app.kafka.topics.order-events}") String orderEventsTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.orderEventsTopic = orderEventsTopic;
    }

    public void publishOrderCreatedEvent(OrderCreatedEvent event) {
        String messageKey = String.valueOf(event.getOrderId());
        log.info("Publishing OrderCreatedEvent: eventId={}, orderId={}", event.getEventId(), event.getOrderId());

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(orderEventsTopic, messageKey, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish OrderCreatedEvent: eventId={}, error={}",
                        event.getEventId(), ex.getMessage());
            } else {
                log.info("Kafka event published: eventId={}, partition={}, offset={}",
                        event.getEventId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
