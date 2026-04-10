package com.example.ecommerce.service;

import com.example.ecommerce.model.Cart;
import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.User;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.CartItemRepository;
import com.example.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void addItem(Product product, String identity, boolean isUsername) {
        Cart cart = getOrCreateCart(identity, isUsername);
        Optional<CartItem> existingItem = cartItemRepository.findByCart_IdAndProduct_Id(cart.getId(), product.getId());

        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + 1);
            cartItemRepository.save(cartItem);
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(1);
            cartItemRepository.save(item);
        }
    }

    public List<CartItem> getItems(String identity, boolean isUsername) {
        return resolveCart(identity, isUsername)
                .map(cart -> cartItemRepository.findByCart_Id(cart.getId()))
                .orElse(List.of());
    }

    @Transactional
    public void removeItem(Long productId, String identity, boolean isUsername) {
        resolveCart(identity, isUsername)
                .flatMap(cart -> cartItemRepository.findByCart_IdAndProduct_Id(cart.getId(), productId))
                .ifPresent(cartItemRepository::delete);
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
        resolveCart(identity, isUsername).ifPresent(cart -> cartItemRepository.deleteByCart_Id(cart.getId()));
    }

    @Transactional
    public void mergeCart(String sessionId, String username) {
        Optional<Cart> guestCartOptional = cartRepository.findBySessionId(sessionId);
        if (guestCartOptional.isEmpty()) {
            return;
        }

        Cart guestCart = guestCartOptional.get();
        Cart userCart = getOrCreateCart(username, true);
        List<CartItem> guestItems = cartItemRepository.findByCart_Id(guestCart.getId());
        for (CartItem guestItem : guestItems) {
            Optional<CartItem> userItem = cartItemRepository.findByCart_IdAndProduct_Id(userCart.getId(), guestItem.getProductId());
            if (userItem.isPresent()) {
                CartItem existing = userItem.get();
                existing.setQuantity(existing.getQuantity() + guestItem.getQuantity());
                cartItemRepository.save(existing);
                cartItemRepository.delete(guestItem);
            } else {
                guestItem.setCart(userCart);
                cartItemRepository.save(guestItem);
            }
        }
        cartRepository.delete(guestCart);
    }

    private Optional<Cart> resolveCart(String identity, boolean isUsername) {
        return isUsername ? cartRepository.findByUserUsername(identity) : cartRepository.findBySessionId(identity);
    }

    private Cart getOrCreateCart(String identity, boolean isUsername) {
        return resolveCart(identity, isUsername).orElseGet(() -> {
            Cart cart = new Cart();
            if (isUsername) {
                User user = userRepository.findByUsername(identity)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                cart.setUser(user);
            } else {
                cart.setSessionId(identity);
            }
            return cartRepository.save(cart);
        });
    }
}
