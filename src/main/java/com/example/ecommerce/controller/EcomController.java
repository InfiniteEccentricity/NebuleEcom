package com.example.ecommerce.controller;

import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.User;
import com.example.ecommerce.service.CartService;
import com.example.ecommerce.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
public class EcomController {

    private final CartService cartService;
    private final UserService userService;

    public EcomController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    private List<Product> getProducts() {
        return Arrays.asList(
            new Product(1, "Nebula Pro Smartphone", 74699.00, "Electronics", "/images/phone.png", 4.8),
            new Product(2, "Quantum Laptop 15\"", 107817.00, "Electronics", "/images/laptop.png", 4.9),
            new Product(3, "Sonic Wireless Headphones", 12409.00, "Electronics", "/images/headphones.png", 4.7),
            new Product(4, "Elevate Series Watch", 24817.00, "Wearables", "/images/watch.png", 4.5),
            new Product(5, "Aura Smart Lamp", 6639.00, "Home Decor", "/images/lamp.png", 4.6),
            new Product(6, "Titan Gaming Mouse", 4979.00, "Accessories", "/images/mouse.png", 4.4)
        );
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

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        String identity = getIdentity(session);
        boolean isUser = isUsername();
        
        // Merge cart if user just logged in
        if (isUser) {
            cartService.mergeCart(session.getId(), identity);
        }

        model.addAttribute("products", getProducts());
        model.addAttribute("categories", Arrays.asList("Electronics", "Wearables", "Home Decor", "Accessories", "Fashion"));
        model.addAttribute("cartCount", cartService.getItems(identity, isUser).size());
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(User user) {
        userService.registerUser(user);
        return "redirect:/login";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam int productId, HttpSession session) {
        String identity = getIdentity(session);
        boolean isUser = isUsername();

        Optional<Product> product = getProducts().stream()
                .filter(p -> p.getId() == productId)
                .findFirst();
        
        product.ifPresent(p -> {
            CartItem item = new CartItem(null, null, null, p.getId(), p.getName(), p.getPrice(), 1, p.getImageUrl());
            cartService.addItem(item, identity, isUser);
        });
        
        return "redirect:/cart";
    }

    @GetMapping("/cart")
    public String viewCart(Model model, HttpSession session) {
        String identity = getIdentity(session);
        boolean isUser = isUsername();
        model.addAttribute("items", cartService.getItems(identity, isUser));
        model.addAttribute("total", cartService.getTotalAmount(identity, isUser));
        return "cart";
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam int productId, HttpSession session) {
        cartService.removeItem(productId, getIdentity(session), isUsername());
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String checkout(Model model, HttpSession session) {
        String identity = getIdentity(session);
        boolean isUser = isUsername();
        if (cartService.getItems(identity, isUser).isEmpty()) {
            return "redirect:/cart";
        }
        model.addAttribute("total", cartService.getTotalAmount(identity, isUser));
        return "checkout";
    }

    @PostMapping("/checkout/pay")
    public String processPayment(HttpSession session) {
        cartService.clear(getIdentity(session), isUsername());
        return "redirect:/payment-success";
    }

    @GetMapping("/payment-success")
    public String paymentSuccess() {
        return "payment-success";
    }

    @GetMapping("/account")
    public String account(Model model, HttpSession session) {
        String username = getIdentity(session);
        if (!isUsername()) {
            return "redirect:/login";
        }
        
        Optional<User> user = userService.findByUsername(username);
        user.ifPresent(u -> model.addAttribute("user", u));
        model.addAttribute("cartCount", cartService.getItems(username, true).size());
        return "account";
    }

    @PostMapping("/account/update")
    public String updateProfile(@RequestParam String email, 
                                @RequestParam(required = false) String newPassword, 
                                HttpSession session) {
        if (!isUsername()) {
            return "redirect:/login";
        }
        
        userService.updateProfile(getIdentity(session), email, newPassword);
        return "redirect:/account?success";
    }
}
