package com.example.ecommerce.repository;

import com.example.ecommerce.model.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @EntityGraph(attributePaths = "items")
    Optional<Order> findByOrderNumber(String orderNumber);

    @EntityGraph(attributePaths = "items")
    List<Order> findByUserUsernameOrderByCreatedAtDesc(String username);

    @org.springframework.data.jpa.repository.Query(value = 
        "SELECT id, order_number, customer_name, total_amount, status, created_at " +
        "FROM orders ORDER BY created_at DESC", nativeQuery = true)
    java.util.List<java.util.Map<String, Object>> findAllOrdersNative();

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = 
        "UPDATE orders SET status = :status WHERE id = :id", nativeQuery = true)
    void updateOrderStatus(@org.springframework.data.repository.query.Param("id") Long id, 
                           @org.springframework.data.repository.query.Param("status") String status);
}
