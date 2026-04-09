package com.example.ecommerce.service;

import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.repository.CartItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {
    
    private final CartItemRepository cartItemRepository;

    public CartService(CartItemRepository cartItemRepository) {
        this.cartItemRepository = cartItemRepository;
    }

    public void addItem(CartItem item, String identity, boolean isUsername) {
        Optional<CartItem> existingItem = Optional.empty();
        if (isUsername) {
            item.setUsername(identity);
            existingItem = cartItemRepository.findByUsernameAndProductId(identity, item.getProductId());
        } else {
            item.setSessionId(identity);
            existingItem = cartItemRepository.findBySessionIdAndProductId(identity, item.getProductId());
        }

        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + item.getQuantity());
            cartItemRepository.save(cartItem);
        } else {
            cartItemRepository.save(item);
        }
    }

    public List<CartItem> getItems(String identity, boolean isUsername) {
        if (isUsername) {
            return cartItemRepository.findByUsername(identity);
        } else {
            return cartItemRepository.findBySessionId(identity);
        }
    }

    @Transactional
    public void removeItem(Long productId, String identity, boolean isUsername) {
        Optional<CartItem> item = Optional.empty();
        if (isUsername) {
            item = cartItemRepository.findByUsernameAndProductId(identity, productId);
        } else {
            item = cartItemRepository.findBySessionIdAndProductId(identity, productId);
        }
        item.ifPresent(cartItemRepository::delete);
    }

    public double getTotalAmount(String identity, boolean isUsername) {
        List<CartItem> items = getItems(identity, isUsername);
        double total = 0;
        for (CartItem item : items) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }

    @Transactional
    public void clear(String identity, boolean isUsername) {
        if (isUsername) {
            cartItemRepository.deleteByUsername(identity);
        } else {
            cartItemRepository.deleteBySessionId(identity);
        }
    }

    @Transactional
    public void mergeCart(String sessionId, String username) {
        List<CartItem> guestItems = cartItemRepository.findBySessionId(sessionId);
        for (CartItem guestItem : guestItems) {
            Optional<CartItem> userItem = cartItemRepository.findByUsernameAndProductId(username, guestItem.getProductId());
            if (userItem.isPresent()) {
                CartItem existing = userItem.get();
                existing.setQuantity(existing.getQuantity() + guestItem.getQuantity());
                cartItemRepository.save(existing);
                cartItemRepository.delete(guestItem);
            } else {
                guestItem.setSessionId(null);
                guestItem.setUsername(username);
                cartItemRepository.save(guestItem);
            }
        }
    }
}
