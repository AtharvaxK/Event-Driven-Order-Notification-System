package com.example.ordernotification.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.mail.mock", havingValue = "true", matchIfMissing = true)
public class MockNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(MockNotificationSender.class);

    @Override
    public void send(String to, String subject, String body) {
        log.info("========================================");
        log.info("[MOCK EMAIL] To: {}", to);
        log.info("[MOCK EMAIL] Subject: {}", subject);
        log.info("[MOCK EMAIL] Body:\n{}", body);
        log.info("========================================");
    }
}
