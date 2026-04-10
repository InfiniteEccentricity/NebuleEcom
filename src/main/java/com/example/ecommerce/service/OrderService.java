package com.example.ecommerce.service;

import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.OrderItem;
import com.example.ecommerce.model.User;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderService(CartService cartService, OrderRepository orderRepository, UserRepository userRepository) {
        this.cartService = cartService;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Order placeOrder(String identity, boolean isUsername, ShippingDetails shippingDetails,
                            String paymentOrderId, String paymentId) {
        List<CartItem> cartItems = cartService.getItems(identity, isUsername);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Cannot place an order with an empty cart.");
        }

        double subtotal = cartItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        double taxAmount = subtotal * 0.18;
        double totalAmount = subtotal + taxAmount;

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        if (isUsername) {
            User user = userRepository.findByUsername(identity)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            order.setUser(user);
        }
        order.setSessionId(isUsername ? null : identity);
        order.setCustomerName(shippingDetails.getFullName());
        order.setEmail(shippingDetails.getEmail());
        order.setAddressLine(shippingDetails.getAddress());
        order.setCity(shippingDetails.getCity());
        order.setState(shippingDetails.getState());
        order.setZipCode(shippingDetails.getZip());
        order.setSubtotal(subtotal);
        order.setTaxAmount(taxAmount);
        order.setTotalAmount(totalAmount);
        order.setStatus("PAID");
        order.setPaymentOrderId(paymentOrderId);
        order.setPaymentId(paymentId);

        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setProductName(cartItem.getProductName());
            orderItem.setPrice(cartItem.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setImageUrl(cartItem.getImageUrl());
            order.addItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order);
        cartService.clear(identity, isUsername);
        return savedOrder;
    }

    public Optional<Order> findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    public List<Order> getOrdersForUser(String username) {
        return orderRepository.findByUserUsernameOrderByCreatedAtDesc(username);
    }

    private String generateOrderNumber() {
        return "NEB-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }

    public static class ShippingDetails {
        private String firstName;
        private String lastName;
        private String email;
        private String address;
        private String city;
        private String state;
        private String zip;

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getState() { return state; }
        public void setState(String state) { this.state = state; }

        public String getZip() { return zip; }
        public void setZip(String zip) { this.zip = zip; }

        public String getFullName() {
            String first = firstName == null ? "" : firstName.trim();
            String last = lastName == null ? "" : lastName.trim();
            return (first + " " + last).trim();
        }
    }
}
