package com.example.ecommerce.repository;

import com.example.ecommerce.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findBySessionId(String sessionId);
    List<CartItem> findByUsername(String username);
    Optional<CartItem> findBySessionIdAndProductId(String sessionId, Long productId);
    Optional<CartItem> findByUsernameAndProductId(String username, Long productId);
    void deleteBySessionId(String sessionId);
    void deleteByUsername(String username);
}
