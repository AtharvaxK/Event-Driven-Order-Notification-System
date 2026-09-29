package com.example.ordernotification.repository;

import com.example.ordernotification.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {}
