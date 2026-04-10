package com.example.ecommerce.repository;

import com.example.ecommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    @org.springframework.data.jpa.repository.Query(value = 
        "SELECT u.id, u.username, u.email, u.created_at, " +
        "(SELECT COUNT(*) FROM orders o WHERE o.user_id = u.id) as order_count " +
        "FROM users u WHERE u.role = 'ROLE_USER' " +
        "ORDER BY u.created_at DESC", nativeQuery = true)
    java.util.List<java.util.Map<String, Object>> findAllCustomersWithOrderCounts();
}
