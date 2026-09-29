package com.example.ordernotification.service;

import com.example.ordernotification.dto.CreateOrderRequest;
import com.example.ordernotification.dto.OrderResponse;
import com.example.ordernotification.dto.UpdateOrderStatusRequest;
import com.example.ordernotification.entity.Order;
import com.example.ordernotification.entity.OrderStatus;
import com.example.ordernotification.entity.User;
import com.example.ordernotification.exception.InvalidStatusTransitionException;
import com.example.ordernotification.exception.OrderNotFoundException;
import com.example.ordernotification.exception.UserNotFoundException;
import com.example.ordernotification.kafka.event.OrderCreatedEvent;
import com.example.ordernotification.kafka.producer.KafkaEventPublisher;
import com.example.ordernotification.repository.OrderRepository;
import com.example.ordernotification.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final KafkaEventPublisher kafkaEventPublisher;

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        KafkaEventPublisher kafkaEventPublisher) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.kafkaEventPublisher = kafkaEventPublisher;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException(request.userId()));

        Order order = orderRepository.save(new Order(request.userId(), request.totalAmount()));
        log.info("Order created: orderId={}", order.getId());

        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID().toString(),
                order.getId(),
                user.getId(),
                user.getEmail(),
                order.getTotalAmount()
        );

        kafkaEventPublisher.publishOrderCreatedEvent(event);

        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long id, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        validateTransition(order.getStatus(), request.status());
        order.setStatus(request.status());
        orderRepository.save(order);
        log.info("Order status updated: orderId={}, status={}", order.getId(), order.getStatus());

        return toResponse(order);
    }

    private void validateTransition(OrderStatus current, OrderStatus next) {
        boolean valid = switch (current) {
            case CREATED -> Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED).contains(next);
            case CONFIRMED -> Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED).contains(next);
            case SHIPPED -> Set.of(OrderStatus.DELIVERED).contains(next);
            case DELIVERED, CANCELLED -> false;
        };

        if (!valid) {
            throw new InvalidStatusTransitionException(current, next);
        }
    }

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(order.getId(), order.getUserId(), order.getTotalAmount(), order.getStatus());
    }
}
