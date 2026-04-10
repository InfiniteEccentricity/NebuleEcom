package com.example.ecommerce.controller;

import com.example.ecommerce.service.CartService;
import com.example.ecommerce.service.OrderService;
import com.example.ecommerce.service.RazorpayService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final RazorpayService razorpayService;
    private final CartService cartService;
    private final OrderService orderService;

    public PaymentController(RazorpayService razorpayService, CartService cartService, OrderService orderService) {
        this.razorpayService = razorpayService;
        this.cartService = cartService;
        this.orderService = orderService;
    }

    private String getIdentity(HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            return auth.getName();
        }
        return session.getId();
    }

    private boolean isUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser");
    }

    @PostMapping("/create-order")
    public ResponseEntity<Map<String, String>> createOrder(@RequestBody Map<String, Object> data, HttpSession session) {
        try {
            String identity = getIdentity(session);
            boolean isUser = isUsername();
            if (cartService.getItems(identity, isUser).isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            double amount = Double.parseDouble(data.get("amount").toString());
            System.out.println("Creating Razorpay order for amount: " + amount);
            String orderId = razorpayService.createOrder(amount);
            System.out.println("Order created successfully: " + orderId);

            session.setAttribute("pendingPaymentOrderId", orderId);
            
            Map<String, String> response = new HashMap<>();
            response.put("orderId", orderId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("ERROR creating Razorpay order: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyPayment(@RequestBody Map<String, String> data, HttpSession session) {
        String orderId = data.get("razorpay_order_id");
        String paymentId = data.get("razorpay_payment_id");
        String signature = data.get("razorpay_signature");

        boolean isValid = razorpayService.verifySignature(orderId, paymentId, signature);

        Map<String, Object> response = new HashMap<>();
        if (isValid) {
            String identity = getIdentity(session);
            boolean isUser = isUsername();

            OrderService.ShippingDetails shippingDetails = new OrderService.ShippingDetails();
            shippingDetails.setFirstName(data.get("firstName"));
            shippingDetails.setLastName(data.get("lastName"));
            shippingDetails.setEmail(data.get("email"));
            shippingDetails.setAddress(data.get("address"));
            shippingDetails.setCity(data.get("city"));
            shippingDetails.setState(data.get("state"));
            shippingDetails.setZip(data.get("zip"));

            String pendingPaymentOrderId = (String) session.getAttribute("pendingPaymentOrderId");
            String paymentOrderId = pendingPaymentOrderId != null ? pendingPaymentOrderId : orderId;
            String savedOrderNumber = orderService.placeOrder(identity, isUser, shippingDetails, paymentOrderId, paymentId)
                    .getOrderNumber();
            session.setAttribute("lastOrderNumber", savedOrderNumber);
            session.removeAttribute("pendingPaymentOrderId");

            response.put("status", "success");
            response.put("orderNumber", savedOrderNumber);
            return ResponseEntity.ok(response);
        } else {
            session.removeAttribute("pendingPaymentOrderId");
            response.put("status", "failed");
            return ResponseEntity.status(400).body(response);
        }
    }
}
